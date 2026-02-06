package com.lastcommit.piilot.domain.regulation.dto.response;

public record ReferenceDocumentDTO(
        String documentTitle,
        String content,
        String article,
        String page,
        Double similarity
) {
}
