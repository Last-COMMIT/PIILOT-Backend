package com.lastcommit.piilot.domain.regulation.dto.response;

import java.util.List;

public record AiRegulationSearchResponseDTO(
        boolean success,
        String context,
        List<PrincipleDTO> principles,
        List<AiReferenceDTO> references
) {
    public record AiReferenceDTO(
            Long documentId,
            String documentName,
            String description,
            Double relevanceScore
    ) {
    }
}
