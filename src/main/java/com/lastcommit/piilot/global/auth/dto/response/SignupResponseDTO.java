package com.lastcommit.piilot.global.auth.dto.response;

import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.entity.UserRole;

public record SignupResponseDTO(
        Long id,
        String email,
        String name,
        UserRole role
) {
    public static SignupResponseDTO from(User user) {
        return new SignupResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
