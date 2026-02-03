package com.lastcommit.piilot.domain.dashboard.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;

import java.time.LocalDateTime;
import java.util.List;

public record RecentFileIssueDTO(
        Long issueId,
        LocalDateTime detectedAt,
        String fileName,
        String connectionName,
        String riskLevel,
        Integer piiCount,
        List<String> piiTypes
) {
    public static RecentFileIssueDTO from(FilePiiIssue issue, List<FilePii> filePiis) {
        var file = issue.getFile();
        var connection = issue.getConnection();

        int totalPiiCount = filePiis.stream()
                .mapToInt(fp -> fp.getTotalPiisCount() != null ?
                        fp.getTotalPiisCount() - (fp.getMaskedPiisCount() != null ? fp.getMaskedPiisCount() : 0) : 0)
                .sum();

        List<String> piiTypeNames = filePiis.stream()
                .map(fp -> fp.getPiiType().getType().getDisplayName())
                .distinct()
                .toList();

        return new RecentFileIssueDTO(
                issue.getId(),
                issue.getDetectedAt(),
                file.getName(),
                connection.getConnectionName(),
                file.getRiskLevel() != null ? file.getRiskLevel().name() : null,
                totalPiiCount,
                piiTypeNames
        );
    }
}
