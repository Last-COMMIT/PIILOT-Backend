package com.lastcommit.piilot.domain.filescan.dto.response;

public record MaskingAiResponseDTO(
        Boolean success,
        String maskedFileBase64
) {
}
