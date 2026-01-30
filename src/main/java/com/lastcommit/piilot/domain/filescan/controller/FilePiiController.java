package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FilePiiControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiSearchCondition;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiFilterOptionDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.filescan.service.FilePiiService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-pii")
@RequiredArgsConstructor
public class FilePiiController implements FilePiiControllerDocs {

    private final FilePiiService filePiiService;

    @Override
    @GetMapping("/summary")
    public CommonResponse<FilePiiSummaryResponseDTO> getSummary(
            @AuthenticationPrincipal Long userId
    ) {
        FilePiiSummaryResponseDTO result = filePiiService.getSummary(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<FilePiiListResponseDTO>> getFilePiiList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) String fileCategory,
            @RequestParam(required = false) Boolean masked,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String keyword
    ) {
        FilePiiSearchCondition condition = new FilePiiSearchCondition(
                connectionId, fileCategory, masked, riskLevel, keyword
        );
        Slice<FilePiiListResponseDTO> result = filePiiService.getFilePiiList(
                userId, condition, PageRequest.of(page, size)
        );
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/connections")
    public CommonResponse<List<FilePiiFilterOptionDTO>> getConnectionOptions(
            @AuthenticationPrincipal Long userId
    ) {
        List<FilePiiFilterOptionDTO> result = filePiiService.getConnectionOptions(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/file-categories")
    public CommonResponse<List<FilePiiFilterOptionDTO>> getFileCategoryOptions() {
        List<FilePiiFilterOptionDTO> result = filePiiService.getFileCategoryOptions();
        return CommonResponse.onSuccess(result);
    }
}
