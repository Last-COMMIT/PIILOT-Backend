package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FileMaskingControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.request.FileMaskingSaveRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.service.FileMaskingService;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-masking")
@RequiredArgsConstructor
public class FileMaskingController implements FileMaskingControllerDocs {

    private final FileMaskingService fileMaskingService;

    @Override
    @GetMapping("/connections")
    public CommonResponse<List<FileMaskingConnectionResponseDTO>> getConnections(
            @AuthenticationPrincipal Long userId
    ) {
        List<FileMaskingConnectionResponseDTO> result = fileMaskingService.getConnections(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/files")
    public CommonResponse<List<FileMaskingFileResponseDTO>> getFiles(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) FileCategory fileCategory,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) String fileName
    ) {
        List<FileMaskingFileResponseDTO> result = fileMaskingService.getIssueFiles(
                userId, connectionId, fileCategory, riskLevel, fileName
        );
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/files/{fileId}/preview")
    public CommonResponse<FileMaskingPreviewResponseDTO> getFilePreview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fileId
    ) {
        FileMaskingPreviewResponseDTO result = fileMaskingService.getFilePreview(userId, fileId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/files/{fileId}/mask")
    public CommonResponse<FileMaskingMaskResponseDTO> maskFile(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fileId
    ) {
        FileMaskingMaskResponseDTO result = fileMaskingService.maskFile(userId, fileId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/files/{fileId}/save")
    public CommonResponse<FileMaskingSaveResponseDTO> saveMaskedFile(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fileId,
            @Valid @RequestBody FileMaskingSaveRequestDTO request
    ) {
        FileMaskingSaveResponseDTO result = fileMaskingService.saveMaskedFile(userId, fileId, request);
        return CommonResponse.onCreated(result);
    }
}
