package com.lastcommit.piilot.domain.document.client;

import com.lastcommit.piilot.domain.document.dto.request.AiEmbeddingRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.AiEmbeddingResponseDTO;
import com.lastcommit.piilot.domain.document.entity.DocumentType;

public interface DocumentEmbeddingAiClient {

    AiEmbeddingResponseDTO requestEmbedding(AiEmbeddingRequestDTO request, DocumentType documentType);
}
