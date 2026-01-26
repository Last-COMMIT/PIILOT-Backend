package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbConnectionRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbmsType;
import com.lastcommit.piilot.domain.dbscan.exception.DbConnectionErrorStatus;
import com.lastcommit.piilot.domain.dbscan.repository.DbServerConnectionRepository;
import com.lastcommit.piilot.domain.dbscan.repository.DbmsTypeRepository;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.error.status.CommonErrorStatus;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbConnectionService {

    private final DbServerConnectionRepository connectionRepository;
    private final DbmsTypeRepository dbmsTypeRepository;
    private final UserRepository userRepository;
    private final DbConnectionTester connectionTester;
    private final AesEncryptor aesEncryptor;

    @Transactional
    public DbConnectionResponseDTO createConnection(Long userId, DbConnectionRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        DbmsType dbmsType = dbmsTypeRepository.findById(request.dbmsTypeId())
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.DBMS_TYPE_NOT_FOUND));

        if (connectionRepository.existsByConnectionNameAndUserId(request.connectionName(), userId)) {
            throw new GeneralException(DbConnectionErrorStatus.CONNECTION_NAME_DUPLICATE);
        }

        boolean success = connectionTester.testConnection(
                dbmsType, request.host(), request.port(),
                request.dbName(), request.username(), request.password()
        );
        ConnectionStatus status = success ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;

        String encryptedPassword = aesEncryptor.encrypt(request.password());

        DbServerConnection connection = DbServerConnection.builder()
                .user(user)
                .dbmsType(dbmsType)
                .connectionName(request.connectionName())
                .host(request.host())
                .port(request.port())
                .dbName(request.dbName())
                .username(request.username())
                .encryptedPassword(encryptedPassword)
                .managerName(request.managerName())
                .managerEmail(request.managerEmail())
                .status(status)
                .build();

        DbServerConnection saved = connectionRepository.save(connection);
        return DbConnectionResponseDTO.from(saved);
    }

    @Transactional
    public DbConnectionResponseDTO updateConnection(Long userId, Long connectionId, DbConnectionRequestDTO request) {
        // 1. 연결 정보 조회
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.CONNECTION_NOT_FOUND));

        // 2. 권한 검증 (본인 연결인지)
        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(DbConnectionErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        // 3. DBMS 유형 조회
        DbmsType dbmsType = dbmsTypeRepository.findById(request.dbmsTypeId())
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.DBMS_TYPE_NOT_FOUND));

        // 4. 연결 이름 중복 검사 (자신의 연결 제외)
        if (!connection.getConnectionName().equals(request.connectionName()) &&
                connectionRepository.existsByConnectionNameAndUserId(request.connectionName(), userId)) {
            throw new GeneralException(DbConnectionErrorStatus.CONNECTION_NAME_DUPLICATE);
        }

        // 5. 연결 테스트
        boolean success = connectionTester.testConnection(
                dbmsType, request.host(), request.port(),
                request.dbName(), request.username(), request.password()
        );
        ConnectionStatus status = success ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;

        // 6. 비밀번호 암호화
        String encryptedPassword = aesEncryptor.encrypt(request.password());

        // 7. 엔티티 업데이트
        connection.updateConnectionInfo(
                dbmsType,
                request.connectionName(),
                request.host(),
                request.port(),
                request.dbName(),
                request.username(),
                encryptedPassword,
                request.managerName(),
                request.managerEmail(),
                status
        );

        return DbConnectionResponseDTO.from(connection);
    }
}
