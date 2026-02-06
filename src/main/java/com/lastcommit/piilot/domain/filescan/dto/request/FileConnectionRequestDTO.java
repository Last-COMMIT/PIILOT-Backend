package com.lastcommit.piilot.domain.filescan.dto.request;

import jakarta.validation.constraints.*;

public record FileConnectionRequestDTO(
        @NotNull(message = "서버 유형은 필수입니다.")
        Integer serverTypeId,

        @NotBlank(message = "연결 이름은 필수입니다.")
        @Size(max = 100, message = "연결 이름은 100자 이하여야 합니다.")
        String connectionName,

        @NotBlank(message = "호스트는 필수입니다.")
        String host,

        @NotNull(message = "포트는 필수입니다.")
        @Min(value = 1, message = "포트는 1 이상이어야 합니다.")
        @Max(value = 65535, message = "포트는 65535 이하여야 합니다.")
        Integer port,

        @NotBlank(message = "기본 경로는 필수입니다.")
        @Size(max = 255, message = "기본 경로는 255자 이하여야 합니다.")
        String defaultPath,

        @NotBlank(message = "사용자명은 필수입니다.")
        @Size(max = 100, message = "사용자명은 100자 이하여야 합니다.")
        String username,

        String password,

        @NotBlank(message = "담당자 이름은 필수입니다.")
        @Size(max = 100, message = "담당자 이름은 100자 이하여야 합니다.")
        String managerName,

        @NotBlank(message = "담당자 이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String managerEmail,

        @NotNull(message = "보존 기간은 필수입니다.")
        @Min(value = 1, message = "보존 기간은 1개월 이상이어야 합니다.")
        Integer retentionPeriodMonths
) {
}
