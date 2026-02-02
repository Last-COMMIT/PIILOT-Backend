package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FilePiiIssueResponseDTO(
        Long issueId,
        String fileName,
        String filePath,
        Integer totalPiiCount,
        List<String> piiTypes,
        RiskLevel riskLevel,
        UserStatus userStatus,
        LocalDateTime detectedAt
) {
    public static FilePiiIssueResponseDTO from(FilePiiIssue issue, List<FilePii> filePiis) {
        File file = issue.getFile();

        // 총 PII 개수 계산
        int totalPiiCount = filePiis.stream()
                .mapToInt(pii -> pii.getTotalPiisCount() != null ? pii.getTotalPiisCount() : 0)
                .sum();

        // PII 유형 한글명 목록
        List<String> piiTypeNames = filePiis.stream()
                .map(pii -> pii.getPiiType().getType().getDisplayName())
                .distinct()
                .toList();

        return new FilePiiIssueResponseDTO(
                issue.getId(),
                file.getName(),
                file.getFilePath(),
                totalPiiCount,
                piiTypeNames,
                file.getRiskLevel(),
                issue.getUserStatus(),
                issue.getDetectedAt()
        );
    }
}
