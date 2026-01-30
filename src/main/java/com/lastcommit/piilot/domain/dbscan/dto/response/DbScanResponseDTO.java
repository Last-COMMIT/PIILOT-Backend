package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbScanHistory;

import java.time.LocalDateTime;

public record DbScanResponseDTO(
        Long scanHistoryId,
        Long connectionId,
        String status,
        LocalDateTime scanStartTime,
        LocalDateTime scanEndTime,
        Long totalTablesCount,
        Long totalColumnsCount,
        Long scannedColumnsCount
) {
    public static DbScanResponseDTO from(DbScanHistory history) {
        return new DbScanResponseDTO(
                history.getId(),
                history.getDbServerConnection().getId(),
                history.getStatus().name(),
                history.getScanStartTime(),
                history.getScanEndTime(),
                history.getTotalTablesCount(),
                history.getTotalColumnsCount(),
                history.getScannedColumnsCount()
        );
    }
}
