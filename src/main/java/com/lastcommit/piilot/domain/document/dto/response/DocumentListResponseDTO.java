package com.lastcommit.piilot.domain.document.dto.response;

import com.lastcommit.piilot.domain.document.entity.Document;

import java.time.LocalDateTime;

public record DocumentListResponseDTO(
        Long id,
        String title,
        String type,
        String typeName,
        LocalDateTime createdAt
) {
    public static DocumentListResponseDTO from(Document document) {
        return new DocumentListResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getType().name(),
                document.getType().getDisplayName(),
                document.getCreatedAt()
        );
    }
}
