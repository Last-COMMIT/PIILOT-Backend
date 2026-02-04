package com.lastcommit.piilot.domain.document.dto.response;

import com.lastcommit.piilot.domain.document.entity.Document;
import com.lastcommit.piilot.domain.document.entity.DocumentType;

import java.time.LocalDateTime;

public record DocumentSaveResponseDTO(
        Long documentId,
        String title,
        DocumentType documentType,
        String s3Url,
        boolean embeddingSuccess,
        LocalDateTime createdAt
) {
    public static DocumentSaveResponseDTO from(Document document, boolean embeddingSuccess) {
        return new DocumentSaveResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getType(),
                document.getUrl(),
                embeddingSuccess,
                document.getCreatedAt()
        );
    }
}
