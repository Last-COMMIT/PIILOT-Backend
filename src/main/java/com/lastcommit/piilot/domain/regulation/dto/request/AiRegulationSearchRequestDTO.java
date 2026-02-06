package com.lastcommit.piilot.domain.regulation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRegulationSearchRequestDTO(
        String query,
        @JsonProperty("n_results")
        Integer nResults,
        @JsonProperty("top_n")
        Integer topN
) {
    public static AiRegulationSearchRequestDTO of(String query) {
        return new AiRegulationSearchRequestDTO(query, 10, 5);
    }
}
