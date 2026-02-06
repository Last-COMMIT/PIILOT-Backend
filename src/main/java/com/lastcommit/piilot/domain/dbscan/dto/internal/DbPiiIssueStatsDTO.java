package com.lastcommit.piilot.domain.dbscan.dto.internal;

public record DbPiiIssueStatsDTO(
        Long totalIssues,
        Long highRiskCount,
        Long mediumRiskCount,
        Long lowRiskCount,
        Long totalRecords
) {
    public static DbPiiIssueStatsDTO empty() {
        return new DbPiiIssueStatsDTO(0L, 0L, 0L, 0L, 0L);
    }
}
