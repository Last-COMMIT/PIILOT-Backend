package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiStatsDTO;

public record FilePiiStatsResponseDTO(
        Long totalFiles,
        Long highRiskCount,
        Double maskingRate,
        Long totalFileSize
) {
    public static FilePiiStatsResponseDTO of(FilePiiStatsDTO stats, Long maskedFileCount) {
        double maskingRate = stats.totalFiles() > 0
                ? (double) maskedFileCount / stats.totalFiles() * 100.0
                : 0.0;

        return new FilePiiStatsResponseDTO(
                stats.totalFiles(),
                stats.highRiskCount(),
                Math.round(maskingRate * 10.0) / 10.0,
                stats.totalFileSize()
        );
    }
}
