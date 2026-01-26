package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

public record DbConnectionDetailResponseDTO(
        Long id,
        String connectionName,
        ConnectionStatus status,
        String dbmsTypeName,
        String host,
        Integer port,
        String dbName,
        String username,
        String password,
        String managerName,
        String managerEmail,
        Long totalTables,
        Long totalColumns
) {
    public static DbConnectionDetailResponseDTO of(DbServerConnection entity, String decryptedPassword,
                                                    Long totalTables, Long totalColumns) {
        return new DbConnectionDetailResponseDTO(
                entity.getId(),
                entity.getConnectionName(),
                entity.getStatus(),
                entity.getDbmsType().getName(),
                entity.getHost(),
                entity.getPort(),
                entity.getDbName(),
                entity.getUsername(),
                decryptedPassword,
                entity.getManagerName(),
                entity.getManagerEmail(),
                totalTables,
                totalColumns
        );
    }
}
