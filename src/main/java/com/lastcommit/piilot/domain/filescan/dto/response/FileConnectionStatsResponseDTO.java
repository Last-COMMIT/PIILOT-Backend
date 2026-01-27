package com.lastcommit.piilot.domain.filescan.dto.response;

public record FileConnectionStatsResponseDTO(
        long totalConnections,
        long activeConnections,
        long totalFiles,
        long totalFileSize
) {
}
