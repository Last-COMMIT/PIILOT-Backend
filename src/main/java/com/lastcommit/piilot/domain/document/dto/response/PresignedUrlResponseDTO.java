package com.lastcommit.piilot.domain.document.dto.response;

public record PresignedUrlResponseDTO(
        String presignedUrl,
        String s3Key,
        String s3Url,
        int expirationMinutes
) {
}
