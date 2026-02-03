package com.lastcommit.piilot.domain.dashboard.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;

import java.time.LocalDateTime;
import java.util.List;

public record RecentDbIssueDTO(
        Long issueId,
        LocalDateTime detectedAt,
        String tableName,
        String columnName,
        String connectionName,
        String riskLevel,
        Long piiCount,
        List<String> piiTypes
) {
    public static RecentDbIssueDTO from(DbPiiIssue issue) {
        var column = issue.getDbPiiColumn();
        var table = column.getDbTable();
        var connection = issue.getConnection();

        return new RecentDbIssueDTO(
                issue.getId(),
                issue.getDetectedAt(),
                table.getName(),
                column.getName(),
                connection.getConnectionName(),
                column.getRiskLevel() != null ? column.getRiskLevel().name() : null,
                column.getTotalRecordsCount() != null ?
                        column.getTotalRecordsCount() - (column.getEncRecordsCount() != null ? column.getEncRecordsCount() : 0) : 0L,
                List.of(column.getPiiType().getType().getDisplayName())
        );
    }
}
