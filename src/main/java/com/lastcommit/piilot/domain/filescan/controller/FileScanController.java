package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FileScanControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanStatusResponseDTO;
import com.lastcommit.piilot.domain.filescan.service.FileScanService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file-connections")
@RequiredArgsConstructor
public class FileScanController implements FileScanControllerDocs {

    private final FileScanService fileScanService;

    @Override
    @PostMapping("/{connectionId}/scan")
    public CommonResponse<FileScanResponseDTO> startScan(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        FileScanResponseDTO result = fileScanService.startScan(connectionId);
        return CommonResponse.onAccepted(result);
    }

    @Override
    @GetMapping("/{connectionId}/scan/{scanHistoryId}")
    public CommonResponse<FileScanStatusResponseDTO> getScanStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId,
            @PathVariable Long scanHistoryId
    ) {
        FileScanStatusResponseDTO result = fileScanService.getScanStatus(connectionId, scanHistoryId);
        return CommonResponse.onSuccess(result);
    }
}
