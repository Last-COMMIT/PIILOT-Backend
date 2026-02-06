package com.lastcommit.piilot.domain.dashboard.dto.response;

public record MonthlyIssueTrendDTO(
        String yearMonth,
        Long issueCount
) {
    public static MonthlyIssueTrendDTO of(String yearMonth, long issueCount) {
        return new MonthlyIssueTrendDTO(yearMonth, issueCount);
    }
}
