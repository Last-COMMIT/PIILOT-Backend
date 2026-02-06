package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;

public record DbPiiConnectionResponseDTO(
        Long id,
        String connectionName,
        String dbmsTypeName
) {
    public static DbPiiConnectionResponseDTO from(DbServerConnection connection) {
        return new DbPiiConnectionResponseDTO(
                connection.getId(),
                connection.getConnectionName(),
                connection.getDbmsType().getName()
        );
    }
}
