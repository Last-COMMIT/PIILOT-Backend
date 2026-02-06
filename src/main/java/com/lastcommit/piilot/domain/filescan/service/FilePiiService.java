package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiStatsDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.exception.FilePiiErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilePiiService {

    private final FileServerConnectionRepository connectionRepository;
    private final FileRepository fileRepository;
    private final FilePiiRepository filePiiRepository;

    public List<FilePiiConnectionResponseDTO> getConnections(Long userId) {
        List<FileServerConnection> connections = connectionRepository.findByUserIdOrderByConnectionNameAsc(userId);
        return connections.stream()
                .map(FilePiiConnectionResponseDTO::from)
                .toList();
    }

    public FilePiiListResponseDTO getFiles(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    ) {
        if (connectionId != null) {
            validateConnectionAccess(userId, connectionId);
        }

        FilePiiStatsDTO stats = fileRepository.calculateStats(
                userId, connectionId, category, masked, riskLevel, keyword
        );

        Long maskedFileCount = fileRepository.countMaskedFiles(
                userId, connectionId, category, masked, riskLevel, keyword
        );

        Slice<File> files = fileRepository.findFilesWithFilters(
                userId, connectionId, category, masked, riskLevel, keyword, pageable
        );

        // File ID 목록 추출
        List<Long> fileIds = files.getContent().stream()
                .map(File::getId)
                .toList();

        // FilePii를 한 번에 조회 (N+1 방지)
        Map<Long, List<FilePii>> filePiiMap = filePiiRepository.findByFileIdIn(fileIds).stream()
                .collect(Collectors.groupingBy(filePii -> filePii.getFile().getId()));

        Slice<FilePiiResponseDTO> content = files.map(file ->
                FilePiiResponseDTO.from(file, filePiiMap.getOrDefault(file.getId(), List.of()))
        );

        FilePiiStatsResponseDTO statsResponse = FilePiiStatsResponseDTO.of(stats, maskedFileCount);

        return FilePiiListResponseDTO.of(statsResponse, content);
    }

    private void validateConnectionAccess(Long userId, Long connectionId) {
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FilePiiErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(FilePiiErrorStatus.CONNECTION_ACCESS_DENIED);
        }
    }
}
