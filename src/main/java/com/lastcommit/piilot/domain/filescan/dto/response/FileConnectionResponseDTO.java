package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

import java.time.LocalDateTime;

public record FileConnectionResponseDTO(
        Long id,
        String connectionName,
        ConnectionStatus status,
        LocalDateTime createdAt
) {
    public static FileConnectionResponseDTO from(FileServerConnection entity) {
        return new FileConnectionResponseDTO(
                entity.getId(),
                entity.getConnectionName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
