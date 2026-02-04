package com.lastcommit.piilot.domain.document.controller;

import com.lastcommit.piilot.domain.document.docs.AdminDocumentControllerDocs;
import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.domain.document.service.DocumentService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController implements AdminDocumentControllerDocs {

    private final DocumentService documentService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<DocumentListResponseDTO> upload(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("type") String type
    ) {
        DocumentListResponseDTO result = documentService.upload(userId, file, type);
        return CommonResponse.onCreated(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<DocumentListResponseDTO>> getDocumentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Slice<DocumentListResponseDTO> result = documentService.getDocumentList(PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }

    @Override
    @DeleteMapping("/{documentId}")
    public CommonResponse<Void> delete(
            @PathVariable Long documentId
    ) {
        documentService.delete(documentId);
        return CommonResponse.onSuccess(null);
    }
}
