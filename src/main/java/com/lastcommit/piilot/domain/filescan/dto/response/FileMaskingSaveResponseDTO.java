package com.lastcommit.piilot.domain.filescan.dto.response;

public record FileMaskingSaveResponseDTO(
        Long fileId,
        String originalZipPath,
        String maskedFilePath,
        String message
) {
    public static FileMaskingSaveResponseDTO of(
            Long fileId,
            String originalZipPath,
            String maskedFilePath
    ) {
        return new FileMaskingSaveResponseDTO(
                fileId,
                originalZipPath,
                maskedFilePath,
                "마스킹이 완료되었습니다."
        );
    }
}
