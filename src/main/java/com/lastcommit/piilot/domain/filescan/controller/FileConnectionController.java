package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FileConnectionControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.request.FileConnectionRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionDetailResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionResponseDTO;
import com.lastcommit.piilot.domain.filescan.service.FileConnectionService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file-connections")
@RequiredArgsConstructor
public class FileConnectionController implements FileConnectionControllerDocs {

    private final FileConnectionService fileConnectionService;

    @Override
    @PostMapping
    public CommonResponse<FileConnectionResponseDTO> createConnection(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FileConnectionRequestDTO request
    ) {
        FileConnectionResponseDTO result = fileConnectionService.createConnection(userId, request);
        return CommonResponse.onCreated(result);
    }

    @Override
    @PutMapping("/{connectionId}")
    public CommonResponse<FileConnectionResponseDTO> updateConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId,
            @Valid @RequestBody FileConnectionRequestDTO request
    ) {
        FileConnectionResponseDTO result = fileConnectionService.updateConnection(userId, connectionId, request);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @DeleteMapping("/{connectionId}")
    public CommonResponse<Void> deleteConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        fileConnectionService.deleteConnection(userId, connectionId);
        return CommonResponse.onSuccess(null);
    }

    @Override
    @GetMapping("/{connectionId}")
    public CommonResponse<FileConnectionDetailResponseDTO> getConnectionDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        FileConnectionDetailResponseDTO result = fileConnectionService.getConnectionDetail(userId, connectionId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<FileConnectionListResponseDTO>> getConnectionList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<FileConnectionListResponseDTO> result = fileConnectionService.getConnectionList(userId, PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }
}
