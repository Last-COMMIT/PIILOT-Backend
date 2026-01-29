package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbPiiIssueControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiIssueStatusUpdateRequest;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueStatusResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueTableResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbPiiIssueService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db-pii-issues")
@RequiredArgsConstructor
public class DbPiiIssueController implements DbPiiIssueControllerDocs {

    private final DbPiiIssueService dbPiiIssueService;

    @Override
    @GetMapping("/summary")
    public CommonResponse<DbPiiIssueSummaryResponseDTO> getSummary(
            @AuthenticationPrincipal Long userId
    ) {
        DbPiiIssueSummaryResponseDTO result = dbPiiIssueService.getSummary(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<DbPiiIssueTableResponseDTO>> getIssueList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Slice<DbPiiIssueTableResponseDTO> result = dbPiiIssueService.getIssueList(userId, PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PatchMapping("/{issueId}/status")
    public CommonResponse<DbPiiIssueStatusResponseDTO> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueId,
            @Valid @RequestBody DbPiiIssueStatusUpdateRequest request
    ) {
        DbPiiIssueStatusResponseDTO result = dbPiiIssueService.updateStatus(userId, issueId, request);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/{issueId}")
    public CommonResponse<DbPiiIssueDetailResponseDTO> getIssueDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueId
    ) {
        DbPiiIssueDetailResponseDTO result = dbPiiIssueService.getIssueDetail(userId, issueId);
        return CommonResponse.onSuccess(result);
    }
}
