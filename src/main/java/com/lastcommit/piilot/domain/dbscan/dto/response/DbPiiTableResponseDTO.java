package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbTable;

public record DbPiiTableResponseDTO(
        Long id,
        String tableName
) {
    public static DbPiiTableResponseDTO from(DbTable table) {
        return new DbPiiTableResponseDTO(
                table.getId(),
                table.getName()
        );
    }
}
