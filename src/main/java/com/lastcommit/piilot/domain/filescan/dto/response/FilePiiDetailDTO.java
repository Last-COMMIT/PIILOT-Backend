package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;

public record FilePiiDetailDTO(
        String piiTypeName,
        String piiTypeCode,
        Integer count
) {
    public static FilePiiDetailDTO from(FilePii filePii) {
        int unmaskedCount = (filePii.getTotalPiisCount() != null ? filePii.getTotalPiisCount() : 0)
                - (filePii.getMaskedPiisCount() != null ? filePii.getMaskedPiisCount() : 0);

        return new FilePiiDetailDTO(
                filePii.getPiiType().getType().getDisplayName(),
                filePii.getPiiType().getType().name(),
                unmaskedCount
        );
    }
}
