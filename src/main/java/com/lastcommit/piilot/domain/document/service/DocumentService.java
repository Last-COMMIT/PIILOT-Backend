package com.lastcommit.piilot.domain.document.service;

import com.lastcommit.piilot.domain.document.client.DocumentEmbeddingAiClient;
import com.lastcommit.piilot.domain.document.dto.request.AiEmbeddingRequestDTO;
import com.lastcommit.piilot.domain.document.dto.request.DocumentSaveRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.AiEmbeddingResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentSaveResponseDTO;
import com.lastcommit.piilot.domain.document.entity.Document;
import com.lastcommit.piilot.domain.document.exception.DocumentErrorStatus;
import com.lastcommit.piilot.domain.document.repository.DocumentRepository;
import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentEmbeddingAiClient documentEmbeddingAiClient;

    @Transactional
    public DocumentSaveResponseDTO saveDocument(Long userId, DocumentSaveRequestDTO request) {
        log.info("문서 저장 시작 - userId={}, title={}, type={}",
                userId, request.title(), request.documentType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(DocumentErrorStatus.USER_NOT_FOUND));

        Document document = Document.builder()
                .user(user)
                .title(request.title())
                .type(request.documentType())
                .url(request.s3Url())
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("문서 DB 저장 완료 - documentId={}", savedDocument.getId());

        boolean embeddingSuccess = requestAiEmbedding(request);

        log.info("문서 저장 완료 - documentId={}, embeddingSuccess={}",
                savedDocument.getId(), embeddingSuccess);

        return DocumentSaveResponseDTO.from(savedDocument, embeddingSuccess);
    }

    private boolean requestAiEmbedding(DocumentSaveRequestDTO request) {
        try {
            AiEmbeddingRequestDTO aiRequest = new AiEmbeddingRequestDTO(request.s3Url());
            AiEmbeddingResponseDTO response = documentEmbeddingAiClient.requestEmbedding(
                    aiRequest, request.documentType());
            return response.success();
        } catch (Exception e) {
            log.warn("AI 임베딩 요청 실패 (문서 저장은 성공) - s3Url={}: {}",
                    request.s3Url(), e.getMessage());
            return false;
        }
    }

    public List<DocumentListResponseDTO> getDocuments(Long userId) {
        log.info("문서 목록 조회 - userId={}", userId);

        List<Document> documents = documentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return documents.stream()
                .map(DocumentListResponseDTO::from)
                .toList();
    }
}
