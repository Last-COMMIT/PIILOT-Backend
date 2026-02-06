package com.lastcommit.piilot.domain.dashboard.dto.response;

import java.util.List;

public record DashboardTrendResponseDTO(
        List<MonthlyIssueTrendDTO> dbTrend,
        List<MonthlyIssueTrendDTO> fileTrend
) {
}
