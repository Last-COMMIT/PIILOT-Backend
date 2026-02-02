package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileScanHistory;

import java.time.LocalDateTime;

public record FileScanResponseDTO(
        Long scanHistoryId,
        Long connectionId,
        String status,
        LocalDateTime scanStartTime
) {
    public static FileScanResponseDTO from(FileScanHistory history) {
        return new FileScanResponseDTO(
                history.getId(),
                history.getFileServerConnection().getId(),
                history.getStatus().name(),
                history.getScanStartTime()
        );
    }
}
