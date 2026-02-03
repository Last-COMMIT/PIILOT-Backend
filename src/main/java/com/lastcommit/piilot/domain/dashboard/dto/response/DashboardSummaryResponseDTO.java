package com.lastcommit.piilot.domain.dashboard.dto.response;

import java.util.List;

public record DashboardSummaryResponseDTO(
        DashboardStatsDTO stats,
        List<PiiDistributionDTO> piiDistribution,
        List<RecentDbIssueDTO> recentDbIssues,
        List<RecentFileIssueDTO> recentFileIssues
) {
}
