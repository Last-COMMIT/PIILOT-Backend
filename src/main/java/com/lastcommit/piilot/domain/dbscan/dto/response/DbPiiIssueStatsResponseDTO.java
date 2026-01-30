package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiIssueStatsDTO;

public record DbPiiIssueStatsResponseDTO(
        Long totalIssues,
        Long highRiskCount,
        Long mediumRiskCount,
        Long lowRiskCount,
        Long totalRecords
) {
    public static DbPiiIssueStatsResponseDTO from(DbPiiIssueStatsDTO stats) {
        return new DbPiiIssueStatsResponseDTO(
                stats.totalIssues(),
                stats.highRiskCount(),
                stats.mediumRiskCount(),
                stats.lowRiskCount(),
                stats.totalRecords()
        );
    }
}
