package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbPiiIssueControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiIssueStatusUpdateRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueStatusUpdateResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbPiiIssueService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db-pii/issues")
@RequiredArgsConstructor
public class DbPiiIssueController implements DbPiiIssueControllerDocs {

    private final DbPiiIssueService dbPiiIssueService;

    @Override
    @GetMapping
    public CommonResponse<DbPiiIssueListResponseDTO> getIssueList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        DbPiiIssueListResponseDTO result = dbPiiIssueService.getIssueList(
                userId,
                PageRequest.of(page, size)
        );
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

    @Override
    @PatchMapping("/{issueId}/status")
    public CommonResponse<DbPiiIssueStatusUpdateResponseDTO> updateIssueStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueId,
            @Valid @RequestBody DbPiiIssueStatusUpdateRequestDTO request
    ) {
        DbPiiIssueStatusUpdateResponseDTO result = dbPiiIssueService.updateIssueStatus(
                userId, issueId, request.userStatus()
        );
        return CommonResponse.onSuccess(result);
    }
}
