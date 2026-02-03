package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;

public record FilePiiDetailDTO(
        String piiTypeName,
        String piiTypeCode,
        Integer count
) {
    public static FilePiiDetailDTO from(FilePii filePii) {
        int totalCount = filePii.getTotalPiisCount() != null ? filePii.getTotalPiisCount() : 0;
        int maskedCount = filePii.getMaskedPiisCount() != null ? filePii.getMaskedPiisCount() : 0;
        int unmaskedCount = Math.max(0, totalCount - maskedCount);

        return new FilePiiDetailDTO(
                filePii.getPiiType().getType().getDisplayName(),
                filePii.getPiiType().getType().name(),
                unmaskedCount
        );
    }
}
