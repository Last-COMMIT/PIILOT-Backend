package com.lastcommit.piilot.domain.regulation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegulationSearchRequestDTO(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 500, message = "검색어는 500자를 초과할 수 없습니다.")
        String query
) {
}
