package com.lastcommit.piilot.domain.document.dto.response;

import com.lastcommit.piilot.domain.document.entity.Document;
import com.lastcommit.piilot.domain.document.entity.DocumentType;

import java.time.LocalDateTime;

public record DocumentListResponseDTO(
        Long documentId,
        String title,
        DocumentType documentType,
        LocalDateTime createdAt
) {
    public static DocumentListResponseDTO from(Document document) {
        return new DocumentListResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getType(),
                document.getCreatedAt()
        );
    }
}
