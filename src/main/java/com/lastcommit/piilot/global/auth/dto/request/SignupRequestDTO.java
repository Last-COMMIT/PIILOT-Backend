package com.lastcommit.piilot.global.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequestDTO(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 10, max = 16, message = "비밀번호는 10자 이상 16자 이하여야 합니다.")
        @Pattern(regexp = "^(?!.*[()\\<>\"';])(?=.*[a-zA-Z])(?=.*[0-9]|.*[!@#$%^&*_+=\\-\\[\\]{}|\\\\:,.?/~`]).+$",
                message = "비밀번호는 영문/숫자/특수문자 중 2종 이상 조합이어야 하며, ( ) < > \" ' ; 는 사용할 수 없습니다.")
        String password,
        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String passwordConfirm,
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        String name
) {
}
