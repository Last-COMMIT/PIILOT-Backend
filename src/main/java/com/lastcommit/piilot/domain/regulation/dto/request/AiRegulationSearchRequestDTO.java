package com.lastcommit.piilot.domain.regulation.dto.request;

public record AiRegulationSearchRequestDTO(
        String query,
        Long userId,
        int maxReferences
) {
    public static AiRegulationSearchRequestDTO of(String query, Long userId) {
        return new AiRegulationSearchRequestDTO(query, userId, 10);
    }
}
