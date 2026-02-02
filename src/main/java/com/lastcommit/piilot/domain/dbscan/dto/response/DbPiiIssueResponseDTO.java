package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;

public record DbPiiIssueResponseDTO(
        Long issueId,
        String columnName,
        String piiTypeName,
        String piiTypeCode,
        Long totalRecordsCount,
        RiskLevel riskLevel,
        UserStatus userStatus,
        LocalDateTime detectedAt
) {
    public static DbPiiIssueResponseDTO from(DbPiiIssue issue) {
        return new DbPiiIssueResponseDTO(
                issue.getId(),
                issue.getDbPiiColumn().getName(),
                issue.getDbPiiColumn().getPiiType().getType().getDisplayName(),
                issue.getDbPiiColumn().getPiiType().getType().name(),
                issue.getDbPiiColumn().getTotalRecordsCount(),
                issue.getDbPiiColumn().getRiskLevel(),
                issue.getUserStatus(),
                issue.getDetectedAt()
        );
    }
}
