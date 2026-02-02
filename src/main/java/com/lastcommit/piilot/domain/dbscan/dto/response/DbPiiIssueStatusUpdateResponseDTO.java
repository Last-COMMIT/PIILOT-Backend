package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;

public record DbPiiIssueStatusUpdateResponseDTO(
        Long issueId,
        UserStatus userStatus,
        LocalDateTime updatedAt
) {
    public static DbPiiIssueStatusUpdateResponseDTO from(DbPiiIssue issue) {
        return new DbPiiIssueStatusUpdateResponseDTO(
                issue.getId(),
                issue.getUserStatus(),
                issue.getUpdatedAt()
        );
    }
}
