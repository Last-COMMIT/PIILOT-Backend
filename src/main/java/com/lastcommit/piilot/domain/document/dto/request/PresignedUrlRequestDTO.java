package com.lastcommit.piilot.domain.document.dto.request;

import com.lastcommit.piilot.domain.document.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PresignedUrlRequestDTO(
        @NotBlank(message = "파일명은 필수입니다.")
        @Size(max = 255, message = "파일명은 255자를 초과할 수 없습니다.")
        String fileName,

        @NotNull(message = "문서 타입은 필수입니다.")
        DocumentType documentType,

        @NotBlank(message = "Content-Type은 필수입니다.")
        @Size(max = 100, message = "Content-Type은 100자를 초과할 수 없습니다.")
        String contentType
) {
}
