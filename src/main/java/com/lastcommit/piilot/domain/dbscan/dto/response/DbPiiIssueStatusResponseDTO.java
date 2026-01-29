package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;

import java.time.LocalDateTime;

public record DbPiiIssueStatusResponseDTO(
        Long issueId,
        String userStatus,
        LocalDateTime updatedAt
) {
    public static DbPiiIssueStatusResponseDTO from(DbPiiIssue issue) {
        return new DbPiiIssueStatusResponseDTO(
                issue.getId(),
                issue.getUserStatus().name(),
                issue.getUpdatedAt()
        );
    }
}
