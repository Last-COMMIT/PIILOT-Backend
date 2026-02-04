package com.lastcommit.piilot.domain.document.service;

import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.domain.document.entity.Document;
import com.lastcommit.piilot.domain.document.entity.DocumentType;
import com.lastcommit.piilot.domain.document.exception.DocumentErrorStatus;
import com.lastcommit.piilot.domain.document.repository.DocumentRepository;
import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.error.status.CommonErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentFileStorage documentFileStorage;

    @Transactional
    public DocumentListResponseDTO upload(Long userId, MultipartFile file, String type) {
        validateFile(file);
        DocumentType documentType = parseDocumentType(type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        String storedFilename = documentFileStorage.store(file);

        try {
            Document document = Document.builder()
                    .user(user)
                    .title(file.getOriginalFilename())
                    .type(documentType)
                    .url(storedFilename)
                    .build();

            Document saved = documentRepository.save(document);
            return DocumentListResponseDTO.from(saved);
        } catch (Exception e) {
            documentFileStorage.delete(storedFilename);
            throw e;
        }
    }

    public Slice<DocumentListResponseDTO> getDocumentList(Pageable pageable) {
        Slice<Document> documents = documentRepository.findAllWithUserOrderByCreatedAtDesc(pageable);
        return documents.map(DocumentListResponseDTO::from);
    }

    @Transactional
    public void delete(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new GeneralException(DocumentErrorStatus.DOCUMENT_NOT_FOUND));

        documentFileStorage.delete(document.getUrl());
        documentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(DocumentErrorStatus.EMPTY_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GeneralException(DocumentErrorStatus.FILE_SIZE_EXCEEDED);
        }
    }

    private DocumentType parseDocumentType(String type) {
        try {
            return DocumentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new GeneralException(DocumentErrorStatus.INVALID_DOCUMENT_TYPE);
        }
    }
}
