package com.lastcommit.piilot.domain.dbscan.dto.internal;

/**
 * 통계 쿼리 결과 전달용 내부 DTO
 */
public record DbPiiStatsDTO(
        Long totalItems,
        Long highRiskItems,
        Long totalEncRecordsCount,
        Long totalRecordsCount
) {
    public Double calculateEncryptionRate() {
        if (totalRecordsCount == null || totalRecordsCount == 0) {
            return 0.0;
        }
        long encCount = totalEncRecordsCount != null ? totalEncRecordsCount : 0;
        return Math.round((double) encCount / totalRecordsCount * 1000) / 10.0;
    }
}
