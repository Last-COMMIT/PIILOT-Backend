package com.lastcommit.piilot.domain.filescan.dto.request;

import com.lastcommit.piilot.domain.filescan.entity.FileCategory;

public record MaskingAiRequestDTO(
        Long connectionId,
        String filePath,
        FileCategory fileCategory
) {
    public static MaskingAiRequestDTO of(Long connectionId, String filePath, FileCategory fileCategory) {
        return new MaskingAiRequestDTO(connectionId, filePath, fileCategory);
    }
}
