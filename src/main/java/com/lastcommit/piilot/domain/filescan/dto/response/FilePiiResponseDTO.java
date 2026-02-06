package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.shared.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

public record FilePiiResponseDTO(
        Long fileId,
        String connectionName,
        String serverTypeName,
        String fileName,
        String filePath,
        String fileCategory,
        String fileCategoryName,
        Boolean masked,
        RiskLevel riskLevel,
        LocalDateTime lastScannedAt
) {
    public static FilePiiResponseDTO from(File file, List<FilePii> filePiiList) {
        boolean isMasked = filePiiList.stream()
                .allMatch(pii -> pii.getTotalPiisCount().equals(pii.getMaskedPiisCount()));

        String categoryName = getCategoryName(file.getFileType().getType().name());

        return new FilePiiResponseDTO(
                file.getId(),
                file.getConnection().getConnectionName(),
                file.getConnection().getServerType().getName(),
                file.getName(),
                file.getFilePath(),
                file.getFileType().getType().name(),
                categoryName,
                isMasked,
                file.getRiskLevel(),
                file.getLastScannedAt()
        );
    }

    private static String getCategoryName(String category) {
        return switch (category) {
            case "DOCUMENT" -> "문서";
            case "PHOTO" -> "사진";
            case "VIDEO" -> "영상";
            case "AUDIO" -> "음성";
            default -> "기타";
        };
    }
}
