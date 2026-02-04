package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;

public record DbPiiIssueResponseDTO(
        Long issueId,
        String columnName,
        String piiTypeName,
        String piiTypeCode,
        Long unencRecordsCount,
        Long totalRecordsCount,
        RiskLevel riskLevel,
        UserStatus userStatus,
        LocalDateTime detectedAt
) {
    public static DbPiiIssueResponseDTO from(DbPiiIssue issue) {
        DbPiiColumn column = issue.getDbPiiColumn();
        Long total = column.getTotalRecordsCount() != null ? column.getTotalRecordsCount() : 0L;
        Long enc = column.getEncRecordsCount() != null ? column.getEncRecordsCount() : 0L;

        return new DbPiiIssueResponseDTO(
                issue.getId(),
                column.getName(),
                column.getPiiType().getType().getDisplayName(),
                column.getPiiType().getType().name(),
                Math.max(0, total - enc),
                total,
                column.getRiskLevel(),
                issue.getUserStatus(),
                issue.getDetectedAt()
        );
    }
}
