package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;

public record FileMaskingFileResponseDTO(
        Long fileId,
        Long connectionId,
        String connectionName,
        String fileName,
        String filePath,
        FileCategory fileCategory,
        String extension,
        RiskLevel riskLevel
) {
    public static FileMaskingFileResponseDTO from(File file) {
        return new FileMaskingFileResponseDTO(
                file.getId(),
                file.getConnection().getId(),
                file.getConnection().getConnectionName(),
                file.getName(),
                file.getFilePath(),
                file.getFileType().getType(),
                file.getFileType().getExtension(),
                file.getRiskLevel()
        );
    }
}
