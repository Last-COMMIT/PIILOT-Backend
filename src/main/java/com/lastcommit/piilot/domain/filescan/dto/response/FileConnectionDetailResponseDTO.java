package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

public record FileConnectionDetailResponseDTO(
        Long id,
        String connectionName,
        ConnectionStatus status,
        String serverTypeName,
        String host,
        Integer port,
        String defaultPath,
        String username,
        String managerName,
        String managerEmail,
        Integer retentionPeriodMonths,
        Long totalFiles,
        Long totalFileSize
) {
    public static FileConnectionDetailResponseDTO of(FileServerConnection entity, Long totalFiles, Long totalFileSize) {
        return new FileConnectionDetailResponseDTO(
                entity.getId(),
                entity.getConnectionName(),
                entity.getStatus(),
                entity.getServerType().getName(),
                entity.getHost(),
                entity.getPort(),
                entity.getDefaultPath(),
                entity.getUsername(),
                entity.getManagerName(),
                entity.getManagerEmail(),
                entity.getRetentionPeriodMonths(),
                totalFiles,
                totalFileSize
        );
    }
}
