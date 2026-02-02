package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FileConnectionRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionDetailResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionStatsResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.entity.FileServerType;
import com.lastcommit.piilot.domain.filescan.exception.FileConnectionErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.FileRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileServerTypeRepository;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.error.status.CommonErrorStatus;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileConnectionService {

    private static final Set<String> SUPPORTED_SERVER_TYPES = Set.of("FTP", "SFTP", "WEBDAV");

    private final FileServerConnectionRepository connectionRepository;
    private final FileServerTypeRepository serverTypeRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileConnectionTester connectionTester;
    private final AesEncryptor aesEncryptor;

    @Transactional
    public FileConnectionResponseDTO createConnection(Long userId, FileConnectionRequestDTO request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new GeneralException(FileConnectionErrorStatus.PASSWORD_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        FileServerType serverType = serverTypeRepository.findById(request.serverTypeId())
                .orElseThrow(() -> new GeneralException(FileConnectionErrorStatus.SERVER_TYPE_NOT_FOUND));

        if (!SUPPORTED_SERVER_TYPES.contains(serverType.getName().toUpperCase())) {
            throw new GeneralException(FileConnectionErrorStatus.UNSUPPORTED_SERVER_TYPE);
        }

        if (connectionRepository.existsByConnectionNameAndUserId(request.connectionName(), userId)) {
            throw new GeneralException(FileConnectionErrorStatus.CONNECTION_NAME_DUPLICATE);
        }

        boolean success = connectionTester.testConnection(
                serverType.getName(), request.host(), request.port(),
                request.username(), request.password(), request.defaultPath()
        );
        ConnectionStatus status = success ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;

        String encryptedPassword = aesEncryptor.encrypt(request.password());

        FileServerConnection connection = FileServerConnection.builder()
                .serverType(serverType)
                .user(user)
                .connectionName(request.connectionName())
                .host(request.host())
                .port(request.port())
                .defaultPath(request.defaultPath())
                .username(request.username())
                .encryptedPassword(encryptedPassword)
                .managerName(request.managerName())
                .managerEmail(request.managerEmail())
                .status(status)
                .retentionPeriodMonths(request.retentionPeriodMonths())
                .build();

        FileServerConnection saved = connectionRepository.save(connection);
        return FileConnectionResponseDTO.from(saved);
    }

    @Transactional
    public FileConnectionResponseDTO updateConnection(Long userId, Long connectionId, FileConnectionRequestDTO request) {
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FileConnectionErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(FileConnectionErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        FileServerType serverType = serverTypeRepository.findById(request.serverTypeId())
                .orElseThrow(() -> new GeneralException(FileConnectionErrorStatus.SERVER_TYPE_NOT_FOUND));

        if (!SUPPORTED_SERVER_TYPES.contains(serverType.getName().toUpperCase())) {
            throw new GeneralException(FileConnectionErrorStatus.UNSUPPORTED_SERVER_TYPE);
        }

        if (!connection.getConnectionName().equals(request.connectionName()) &&
                connectionRepository.existsByConnectionNameAndUserId(request.connectionName(), userId)) {
            throw new GeneralException(FileConnectionErrorStatus.CONNECTION_NAME_DUPLICATE);
        }

        boolean isPasswordChanged = request.password() != null && !request.password().isBlank();
        String passwordForTest = isPasswordChanged
                ? request.password()
                : aesEncryptor.decrypt(connection.getEncryptedPassword());
        String encryptedPassword = isPasswordChanged
                ? aesEncryptor.encrypt(request.password())
                : connection.getEncryptedPassword();

        boolean success = connectionTester.testConnection(
                serverType.getName(), request.host(), request.port(),
                request.username(), passwordForTest, request.defaultPath()
        );
        ConnectionStatus status = success ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;

        connection.updateConnectionInfo(
                serverType,
                request.connectionName(),
                request.host(),
                request.port(),
                request.defaultPath(),
                request.username(),
                encryptedPassword,
                request.managerName(),
                request.managerEmail(),
                status,
                request.retentionPeriodMonths()
        );

        return FileConnectionResponseDTO.from(connection);
    }

    @Transactional
    public void deleteConnection(Long userId, Long connectionId) {
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FileConnectionErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(FileConnectionErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        connectionRepository.delete(connection);
    }

    public FileConnectionDetailResponseDTO getConnectionDetail(Long userId, Long connectionId) {
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FileConnectionErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(FileConnectionErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        long totalFiles = fileRepository.countByConnectionId(connectionId);
        long totalFileSize = fileRepository.sumFileSizeByConnectionId(connectionId);

        return FileConnectionDetailResponseDTO.of(connection, totalFiles, totalFileSize);
    }

    public FileConnectionStatsResponseDTO getConnectionStats(Long userId) {
        long totalConnections = connectionRepository.countByUserId(userId);
        long activeConnections = connectionRepository.countByUserIdAndStatus(userId, ConnectionStatus.CONNECTED);
        long totalFiles = fileRepository.countByConnectionUserId(userId);
        long totalFileSize = fileRepository.sumFileSizeByConnectionUserId(userId);

        return new FileConnectionStatsResponseDTO(totalConnections, activeConnections, totalFiles, totalFileSize);
    }

    public Slice<FileConnectionListResponseDTO> getConnectionList(Long userId, Pageable pageable) {
        Slice<FileServerConnection> connections = connectionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return connections.map(connection -> {
            long totalFiles = fileRepository.countByConnectionId(connection.getId());
            long totalFileSize = fileRepository.sumFileSizeByConnectionId(connection.getId());
            return FileConnectionListResponseDTO.of(connection, totalFiles, totalFileSize);
        });
    }
}
