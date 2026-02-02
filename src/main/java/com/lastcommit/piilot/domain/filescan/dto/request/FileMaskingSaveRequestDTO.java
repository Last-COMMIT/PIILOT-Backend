package com.lastcommit.piilot.domain.filescan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileMaskingSaveRequestDTO(
        @NotBlank(message = "암호화 비밀번호는 필수입니다.")
        @Size(min = 4, max = 50, message = "비밀번호는 4자 이상 50자 이하여야 합니다.")
        String encryptionPassword
) {
}
