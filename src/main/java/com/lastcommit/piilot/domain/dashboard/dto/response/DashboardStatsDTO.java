package com.lastcommit.piilot.domain.dashboard.dto.response;

public record DashboardStatsDTO(
        Integer totalConnections,
        Integer dbConnectionCount,
        Integer fileConnectionCount,
        Long piiColumnCount,
        Double columnEncryptionRate,
        Long piiFileCount,
        Double fileEncryptionRate,
        Integer totalIssueCount,
        Integer dbIssueCount,
        Integer fileIssueCount
) {
    public static DashboardStatsDTO of(
            long dbCount, long fileCount,
            long piiColumnCount, double columnEncRate,
            long piiFileCount, double fileEncRate,
            long dbIssueCount, long fileIssueCount
    ) {
        return new DashboardStatsDTO(
                (int) (dbCount + fileCount),
                (int) dbCount,
                (int) fileCount,
                piiColumnCount,
                Math.round(columnEncRate * 10.0) / 10.0,
                piiFileCount,
                Math.round(fileEncRate * 10.0) / 10.0,
                (int) (dbIssueCount + fileIssueCount),
                (int) dbIssueCount,
                (int) fileIssueCount
        );
    }
}
