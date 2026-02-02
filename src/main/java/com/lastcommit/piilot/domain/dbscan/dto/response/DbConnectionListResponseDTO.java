package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;

public record DbConnectionListResponseDTO(
        Long id,
        String connectionName,
        String dbmsTypeName,
        ConnectionStatus status,
        String host,
        String dbName,
        Long totalTables,
        Long totalColumns,
        Boolean isScanning
) {
    public static DbConnectionListResponseDTO of(DbServerConnection entity, Long totalTables, Long totalColumns) {
        return new DbConnectionListResponseDTO(
                entity.getId(),
                entity.getConnectionName(),
                entity.getDbmsType().getName(),
                entity.getStatus(),
                entity.getHost() + ":" + entity.getPort(),
                entity.getDbName(),
                totalTables,
                totalColumns,
                entity.getIsScanning()
        );
    }
}
