package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

import java.time.LocalDateTime;

public record DbConnectionResponseDTO(
        Long id,
        String dbmsTypeName,
        String connectionName,
        String host,
        Integer port,
        String dbName,
        String username,
        String managerName,
        String managerEmail,
        ConnectionStatus status,
        LocalDateTime createdAt
) {
    public static DbConnectionResponseDTO from(DbServerConnection entity) {
        return new DbConnectionResponseDTO(
                entity.getId(),
                entity.getDbmsType().getName(),
                entity.getConnectionName(),
                entity.getHost(),
                entity.getPort(),
                entity.getDbName(),
                entity.getUsername(),
                entity.getManagerName(),
                entity.getManagerEmail(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
