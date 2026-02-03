package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;

public record FileMaskingMaskResponseDTO(
        Long fileId,
        String originalFileName,
        String maskedFileName,
        FileCategory fileCategory,
        String mimeType,
        Boolean previewAvailable,
        String maskedContent,
        String previewMessage
) {
    public static FileMaskingMaskResponseDTO available(
            File file,
            String maskedFileName,
            String mimeType,
            String maskedContent
    ) {
        return new FileMaskingMaskResponseDTO(
                file.getId(),
                file.getName(),
                maskedFileName,
                file.getFileType().getType(),
                mimeType,
                true,
                maskedContent,
                null
        );
    }

    public static FileMaskingMaskResponseDTO unavailable(
            File file,
            String maskedFileName,
            String mimeType,
            String message
    ) {
        return new FileMaskingMaskResponseDTO(
                file.getId(),
                file.getName(),
                maskedFileName,
                file.getFileType().getType(),
                mimeType,
                false,
                null,
                message
        );
    }
}
