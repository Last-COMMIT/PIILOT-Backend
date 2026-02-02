package com.lastcommit.piilot.domain.filescan.dto.internal;

import java.time.LocalDateTime;

public record FileMetadataDTO(
        String filePath,
        String fileName,
        String extension,
        Long fileSize,
        LocalDateTime lastModifiedTime
) {
}
