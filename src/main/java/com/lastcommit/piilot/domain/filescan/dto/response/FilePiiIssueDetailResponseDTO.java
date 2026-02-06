package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record FilePiiIssueDetailResponseDTO(
        Long issueId,
        String connectionName,
        String serverTypeName,
        String fileName,
        String filePath,
        String fileExtension,
        String fileCategory,
        String fileCategoryName,
        String mimeType,
        Boolean previewAvailable,
        String fileContent,
        String previewMessage,
        Integer totalPiiCount,
        Integer maskedPiiCount,
        Integer unmaskedPiiCount,
        RiskLevel riskLevel,
        UserStatus userStatus,
        IssueStatus issueStatus,
        LocalDateTime detectedAt,
        String managerName,
        String managerEmail,
        List<FilePiiDetailDTO> piiDetails
) {
    public static FilePiiIssueDetailResponseDTO available(
            FilePiiIssue issue,
            List<FilePii> filePiis,
            String extension,
            String mimeType,
            String base64Content
    ) {
        return build(issue, filePiis, extension, mimeType, true, base64Content, null);
    }

    public static FilePiiIssueDetailResponseDTO unavailable(
            FilePiiIssue issue,
            List<FilePii> filePiis,
            String extension,
            String mimeType,
            String message
    ) {
        return build(issue, filePiis, extension, mimeType, false, null, message);
    }

    private static FilePiiIssueDetailResponseDTO build(
            FilePiiIssue issue,
            List<FilePii> filePiis,
            String extension,
            String mimeType,
            boolean previewAvailable,
            String fileContent,
            String previewMessage
    ) {
        File file = issue.getFile();
        FileServerConnection connection = issue.getConnection();
        List<FilePii> safePiis = filePiis != null ? filePiis : Collections.emptyList();

        // 총 PII 개수 계산
        int totalPiiCount = safePiis.stream()
                .mapToInt(pii -> pii.getTotalPiisCount() != null ? pii.getTotalPiisCount() : 0)
                .sum();

        // 마스킹된 PII 개수 계산
        int maskedPiiCount = safePiis.stream()
                .mapToInt(pii -> pii.getMaskedPiisCount() != null ? pii.getMaskedPiisCount() : 0)
                .sum();

        // PII 상세 정보 (미마스킹 개수가 0보다 큰 것만)
        List<FilePiiDetailDTO> piiDetails = safePiis.stream()
                .map(FilePiiDetailDTO::from)
                .filter(dto -> dto.count() > 0)
                .toList();

        return new FilePiiIssueDetailResponseDTO(
                issue.getId(),
                connection != null ? connection.getConnectionName() : null,
                connection != null && connection.getServerType() != null ? connection.getServerType().getName() : null,
                file.getName(),
                file.getFilePath(),
                extension,
                extension != null ? extension.toUpperCase() : null,
                file.getFileType().getType().getDisplayName(),
                mimeType,
                previewAvailable,
                fileContent,
                previewMessage,
                totalPiiCount,
                maskedPiiCount,
                Math.max(0, totalPiiCount - maskedPiiCount),
                file.getRiskLevel(),
                issue.getUserStatus(),
                issue.getIssueStatus(),
                issue.getDetectedAt(),
                connection != null ? connection.getManagerName() : null,
                connection != null ? connection.getManagerEmail() : null,
                piiDetails
        );
    }
}
