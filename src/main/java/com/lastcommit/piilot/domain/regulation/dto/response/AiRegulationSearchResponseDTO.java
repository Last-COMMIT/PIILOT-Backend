package com.lastcommit.piilot.domain.regulation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiRegulationSearchResponseDTO(
        String answer,
        List<AiReferenceDTO> sources
) {
    public record AiReferenceDTO(
            @JsonProperty("document_title")
            String documentTitle,
            String content,
            String article,
            String page,
            Double similarity
    ) {
    }
}
