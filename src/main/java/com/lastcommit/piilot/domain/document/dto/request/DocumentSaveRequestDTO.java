package com.lastcommit.piilot.domain.document.dto.request;

import com.lastcommit.piilot.domain.document.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentSaveRequestDTO(
        @NotBlank(message = "문서 제목은 필수입니다.")
        @Size(max = 100, message = "문서 제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotNull(message = "문서 타입은 필수입니다.")
        DocumentType documentType,

        @NotBlank(message = "S3 URL은 필수입니다.")
        @Size(max = 255, message = "S3 URL은 255자를 초과할 수 없습니다.")
        String s3Url
) {
}
