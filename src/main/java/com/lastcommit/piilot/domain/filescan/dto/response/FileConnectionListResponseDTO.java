package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

import java.time.LocalDateTime;

public record FileConnectionListResponseDTO(
        Long id,
        String connectionName,
        ConnectionStatus status,
        String serverTypeName,
        String host,
        Long totalFiles,
        LocalDateTime createdAt
) {
    public static FileConnectionListResponseDTO of(FileServerConnection entity, Long totalFiles) {
        return new FileConnectionListResponseDTO(
                entity.getId(),
                entity.getConnectionName(),
                entity.getStatus(),
                entity.getServerType().getName(),
                entity.getHost() + ":" + entity.getPort(),
                totalFiles,
                entity.getCreatedAt()
        );
    }
}
