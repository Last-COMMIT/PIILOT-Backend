package com.lastcommit.piilot.domain.dbscan.dto.response;

public record DbConnectionStatsResponseDTO(
        long totalConnections,
        long activeConnections,
        long totalTables,
        long totalColumns
) {
}
