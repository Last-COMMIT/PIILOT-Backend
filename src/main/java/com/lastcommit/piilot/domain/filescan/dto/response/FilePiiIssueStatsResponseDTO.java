package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiIssueStatsDTO;

public record FilePiiIssueStatsResponseDTO(
        Long totalIssues,
        Long highRiskCount,
        Long mediumRiskCount,
        Long lowRiskCount,
        Long totalPiiCount
) {
    public static FilePiiIssueStatsResponseDTO from(FilePiiIssueStatsDTO stats) {
        return new FilePiiIssueStatsResponseDTO(
                stats.totalIssues(),
                stats.highRiskCount(),
                stats.mediumRiskCount(),
                stats.lowRiskCount(),
                stats.totalPiiCount()
        );
    }
}
