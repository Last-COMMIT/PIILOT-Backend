package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiStatsDTO;

public record DbPiiStatsResponseDTO(
        Long totalItems,
        Long highRiskItems,
        Double encryptionRate,
        Long totalRecords
) {
    public static DbPiiStatsResponseDTO from(DbPiiStatsDTO stats) {
        return new DbPiiStatsResponseDTO(
                stats.totalItems(),
                stats.highRiskItems(),
                stats.calculateEncryptionRate(),
                stats.totalRecordsCount()
        );
    }
}
