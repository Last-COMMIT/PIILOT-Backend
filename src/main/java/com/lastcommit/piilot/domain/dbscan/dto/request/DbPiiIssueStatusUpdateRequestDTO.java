package com.lastcommit.piilot.domain.dbscan.dto.request;

import com.lastcommit.piilot.domain.shared.UserStatus;
import jakarta.validation.constraints.NotNull;

public record DbPiiIssueStatusUpdateRequestDTO(
        @NotNull(message = "작업 상태는 필수입니다.")
        UserStatus userStatus
) {
}
