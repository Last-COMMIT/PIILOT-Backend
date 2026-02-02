package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;

public record FileMaskingPreviewResponseDTO(
        Long fileId,
        String fileName,
        FileCategory fileCategory,
        String mimeType,
        Long fileSize,
        Boolean previewAvailable,
        String content,
        String previewMessage
) {
    public static FileMaskingPreviewResponseDTO available(
            File file,
            String mimeType,
            String base64Content
    ) {
        return new FileMaskingPreviewResponseDTO(
                file.getId(),
                file.getName(),
                file.getFileType().getType(),
                mimeType,
                file.getFileSize(),
                true,
                base64Content,
                null
        );
    }

    public static FileMaskingPreviewResponseDTO unavailable(
            File file,
            String mimeType,
            String message
    ) {
        return new FileMaskingPreviewResponseDTO(
                file.getId(),
                file.getName(),
                file.getFileType().getType(),
                mimeType,
                file.getFileSize(),
                false,
                null,
                message
        );
    }
}
