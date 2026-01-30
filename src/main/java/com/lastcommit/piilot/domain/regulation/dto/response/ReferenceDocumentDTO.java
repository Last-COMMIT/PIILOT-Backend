package com.lastcommit.piilot.domain.regulation.dto.response;

public record ReferenceDocumentDTO(
        Long id,
        String documentName,
        String description,
        String documentUrl
) {
}
