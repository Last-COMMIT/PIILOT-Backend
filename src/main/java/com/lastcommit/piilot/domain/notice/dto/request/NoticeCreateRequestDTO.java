package com.lastcommit.piilot.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequestDTO(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}
