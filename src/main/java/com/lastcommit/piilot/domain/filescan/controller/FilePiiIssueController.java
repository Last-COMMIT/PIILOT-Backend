package com.lastcommit.piilot.domain.filescan.controller;

import com.lastcommit.piilot.domain.filescan.docs.FilePiiIssueControllerDocs;
import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiIssueStatusUpdateRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueStatusUpdateResponseDTO;
import com.lastcommit.piilot.domain.filescan.service.FilePiiIssueService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file-pii/issues")
@RequiredArgsConstructor
@Validated
public class FilePiiIssueController implements FilePiiIssueControllerDocs {

    private final FilePiiIssueService filePiiIssueService;

    @Override
    @GetMapping
    public CommonResponse<FilePiiIssueListResponseDTO> getIssueList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        FilePiiIssueListResponseDTO result = filePiiIssueService.getIssueList(
                userId,
                PageRequest.of(page, size)
        );
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/{issueId}")
    public CommonResponse<FilePiiIssueDetailResponseDTO> getIssueDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueId
    ) {
        FilePiiIssueDetailResponseDTO result = filePiiIssueService.getIssueDetail(userId, issueId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PatchMapping("/{issueId}/status")
    public CommonResponse<FilePiiIssueStatusUpdateResponseDTO> updateIssueStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueId,
            @RequestBody FilePiiIssueStatusUpdateRequestDTO request
    ) {
        FilePiiIssueStatusUpdateResponseDTO result = filePiiIssueService.updateIssueStatus(
                userId, issueId, request.userStatus()
        );
        return CommonResponse.onSuccess(result);
    }
}
