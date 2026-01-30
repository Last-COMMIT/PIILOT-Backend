package com.lastcommit.piilot.domain.filescan.dto.response;

public record FilePiiSummaryResponseDTO(
        long totalFiles,
        long highRiskFiles,
        double maskingRate,
        long totalFileSize,
        String formattedFileSize
) {
    public static FilePiiSummaryResponseDTO of(long totalFiles, long highRiskFiles,
                                                double maskingRate, long totalFileSize) {
        return new FilePiiSummaryResponseDTO(
                totalFiles,
                highRiskFiles,
                maskingRate,
                totalFileSize,
                formatFileSize(totalFileSize)
        );
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        if (gb < 1024) {
            return String.format("%.1f GB", gb);
        }
        double tb = gb / 1024.0;
        return String.format("%.1f TB", tb);
    }
}
