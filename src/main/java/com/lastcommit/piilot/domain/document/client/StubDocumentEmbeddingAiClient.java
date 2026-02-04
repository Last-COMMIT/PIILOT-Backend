package com.lastcommit.piilot.domain.document.client;

import com.lastcommit.piilot.domain.document.dto.request.AiEmbeddingRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.AiEmbeddingResponseDTO;
import com.lastcommit.piilot.domain.document.entity.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("stub")
public class StubDocumentEmbeddingAiClient implements DocumentEmbeddingAiClient {

    @Override
    public AiEmbeddingResponseDTO requestEmbedding(AiEmbeddingRequestDTO request, DocumentType documentType) {
        log.info("[Stub] AI 임베딩 요청 - documentType={}, filePath={}", documentType, request.filePath());

        // Stub always returns success
        return new AiEmbeddingResponseDTO(true, "Stub embedding completed successfully");
    }
}
