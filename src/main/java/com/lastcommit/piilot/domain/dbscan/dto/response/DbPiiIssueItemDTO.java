package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;

public record DbPiiIssueItemDTO(
        Long issueId,
        Long columnId,
        String columnName,
        String piiType,
        String piiTypeName,
        Long totalRecords,
        String riskLevel,
        String userStatus
) {
    public static DbPiiIssueItemDTO from(DbPiiIssue issue) {
        return new DbPiiIssueItemDTO(
                issue.getId(),
                issue.getDbPiiColumn().getId(),
                issue.getDbPiiColumn().getName(),
                issue.getDbPiiColumn().getPiiType().getType().name(),
                issue.getDbPiiColumn().getPiiType().getType().getDisplayName(),
                issue.getDbPiiColumn().getTotalRecordsCount(),
                issue.getDbPiiColumn().getRiskLevel().name(),
                issue.getUserStatus().name()
        );
    }
}
