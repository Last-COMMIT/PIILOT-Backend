package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;

public record FilePiiIssueStatusUpdateResponseDTO(
        Long issueId,
        UserStatus userStatus,
        LocalDateTime updatedAt
) {
    public static FilePiiIssueStatusUpdateResponseDTO from(FilePiiIssue issue) {
        return new FilePiiIssueStatusUpdateResponseDTO(
                issue.getId(),
                issue.getUserStatus(),
                issue.getUpdatedAt()
        );
    }
}
