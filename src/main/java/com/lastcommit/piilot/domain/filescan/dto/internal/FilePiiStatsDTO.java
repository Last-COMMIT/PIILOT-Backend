package com.lastcommit.piilot.domain.filescan.dto.internal;

public record FilePiiStatsDTO(
        Long totalFiles,
        Long highRiskCount,
        Long totalFileSize
) {
}
