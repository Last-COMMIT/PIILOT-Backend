package com.lastcommit.piilot.domain.regulation.dto.response;

import java.util.List;

public record RegulationSearchResponseDTO(
        String answer,
        List<ReferenceDocumentDTO> references,
        int totalReferences
) {
    public static RegulationSearchResponseDTO from(AiRegulationSearchResponseDTO aiResponse) {
        List<ReferenceDocumentDTO> references = aiResponse.sources().stream()
                .map(ref -> new ReferenceDocumentDTO(
                        ref.documentTitle(),
                        ref.content(),
                        ref.article(),
                        ref.page(),
                        ref.similarity()
                ))
                .toList();

        return new RegulationSearchResponseDTO(
                aiResponse.answer(),
                references,
                references.size()
        );
    }
}
