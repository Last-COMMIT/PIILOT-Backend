package com.lastcommit.piilot.domain.dbscan.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DbPiiIssueStatusUpdateRequest(
        @NotBlank(message = "작업 상태는 필수입니다.")
        String userStatus   // ISSUE, RUNNING, DONE
) {}
