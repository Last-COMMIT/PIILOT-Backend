package com.lastcommit.piilot.domain.document.controller;

import com.lastcommit.piilot.domain.document.docs.DocumentControllerDocs;
import com.lastcommit.piilot.domain.document.dto.request.DocumentSaveRequestDTO;
import com.lastcommit.piilot.domain.document.dto.request.PresignedUrlRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentSaveResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.PresignedUrlResponseDTO;
import com.lastcommit.piilot.domain.document.service.DocumentService;
import com.lastcommit.piilot.domain.document.service.S3PresignedUrlService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentControllerDocs {

    private final S3PresignedUrlService s3PresignedUrlService;
    private final DocumentService documentService;

    @Override
    @PostMapping("/presigned-url")
    public CommonResponse<PresignedUrlResponseDTO> generatePresignedUrl(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PresignedUrlRequestDTO request
    ) {
        PresignedUrlResponseDTO result = s3PresignedUrlService.generatePresignedUrl(userId, request);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PostMapping
    public CommonResponse<DocumentSaveResponseDTO> saveDocument(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DocumentSaveRequestDTO request
    ) {
        DocumentSaveResponseDTO result = documentService.saveDocument(userId, request);
        return CommonResponse.onCreated(result);
    }

    @Override
    @GetMapping
    public CommonResponse<List<DocumentListResponseDTO>> getDocuments(
            @AuthenticationPrincipal Long userId
    ) {
        List<DocumentListResponseDTO> result = documentService.getDocuments(userId);
        return CommonResponse.onSuccess(result);
    }
}
