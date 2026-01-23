package com.lastcommit.piilot.global.auth.dto.response;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}
