package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FilePiiControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiConnectionResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.service.FilePiiService;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import com.lastcommit.piilot.global.validation.annotation.ValidPage;
import com.lastcommit.piilot.global.validation.annotation.ValidSize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-pii")
@RequiredArgsConstructor
@Validated
public class FilePiiController implements FilePiiControllerDocs {

    private final FilePiiService filePiiService;

    @Override
    @GetMapping("/connections")
    public CommonResponse<List<FilePiiConnectionResponseDTO>> getConnections(
            @AuthenticationPrincipal Long userId
    ) {
        List<FilePiiConnectionResponseDTO> result = filePiiService.getConnections(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/files")
    public CommonResponse<FilePiiListResponseDTO> getFiles(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) FileCategory category,
            @RequestParam(required = false) Boolean masked,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) String keyword,
            @ValidPage @RequestParam(defaultValue = "0") int page,
            @ValidSize @RequestParam(defaultValue = "20") int size
    ) {
        FilePiiListResponseDTO result = filePiiService.getFiles(
                userId, connectionId, category, masked, riskLevel, keyword,
                PageRequest.of(page, size)
        );
        return CommonResponse.onSuccess(result);
    }
}
