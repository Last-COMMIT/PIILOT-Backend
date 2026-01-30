package com.lastcommit.piilot.domain.filescan.dto.response;

import java.time.LocalDateTime;

public record FilePiiListResponseDTO(
        Long id,
        String connectionName,
        String fileName,
        String filePath,
        String fileCategory,
        String fileCategoryName,
        String maskingStatus,
        Double maskingRate,
        String riskLevel,
        LocalDateTime lastScannedAt
) {
}
