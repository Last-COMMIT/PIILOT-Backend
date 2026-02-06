package com.lastcommit.piilot.domain.filescan.dto.internal;

public record FilePiiIssueStatsDTO(
        Long totalIssues,
        Long highRiskCount,
        Long mediumRiskCount,
        Long lowRiskCount,
        Long totalPiiCount
) {
    public static FilePiiIssueStatsDTO empty() {
        return new FilePiiIssueStatsDTO(0L, 0L, 0L, 0L, 0L);
    }
}
