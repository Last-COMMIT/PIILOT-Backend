package com.lastcommit.piilot.domain.document.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiEmbeddingRequestDTO(
        @JsonProperty("file_path")
        String filePath
) {
}
