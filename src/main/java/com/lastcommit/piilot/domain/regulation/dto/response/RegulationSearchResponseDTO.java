package com.lastcommit.piilot.domain.regulation.dto.response;

import java.util.List;

public record RegulationSearchResponseDTO(
        String context,
        List<PrincipleDTO> principles,
        List<ReferenceDocumentDTO> references,
        int totalReferences
) {
    public static RegulationSearchResponseDTO from(AiRegulationSearchResponseDTO aiResponse) {
        List<ReferenceDocumentDTO> references = aiResponse.references().stream()
                .map(ref -> new ReferenceDocumentDTO(
                        ref.documentId(),
                        ref.documentName(),
                        ref.description(),
                        "/api/documents/" + ref.documentId()
                ))
                .toList();

        return new RegulationSearchResponseDTO(
                aiResponse.context(),
                aiResponse.principles(),
                references,
                references.size()
        );
    }
}
