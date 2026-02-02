package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileScanHistory;

import java.time.LocalDateTime;

public record FileScanStatusResponseDTO(
        Long scanHistoryId,
        Long connectionId,
        String status,
        LocalDateTime scanStartTime,
        LocalDateTime scanEndTime,
        Long totalFilesCount,
        Long totalFilesSize,
        Long scannedFilesCount
) {
    public static FileScanStatusResponseDTO from(FileScanHistory history) {
        return new FileScanStatusResponseDTO(
                history.getId(),
                history.getFileServerConnection().getId(),
                history.getStatus().name(),
                history.getScanStartTime(),
                history.getScanEndTime(),
                history.getTotalFilesCount(),
                history.getTotalFilesSize(),
                history.getScannedFilesCount()
        );
    }
}
