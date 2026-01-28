package com.lastcommit.piilot.domain.notice.controller;

import com.lastcommit.piilot.domain.notice.docs.AdminNoticeControllerDocs;
import com.lastcommit.piilot.domain.notice.dto.request.NoticeCreateRequestDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeResponseDTO;
import com.lastcommit.piilot.domain.notice.service.NoticeService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController implements AdminNoticeControllerDocs {

    private final NoticeService noticeService;

    @Override
    @PostMapping
    public CommonResponse<NoticeResponseDTO> createNotice(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NoticeCreateRequestDTO request
    ) {
        NoticeResponseDTO result = noticeService.createNotice(userId, request);
        return CommonResponse.onCreated(result);
    }

    @Override
    @DeleteMapping("/{noticeId}")
    public CommonResponse<Void> deleteNotice(
            @PathVariable Long noticeId
    ) {
        noticeService.deleteNotice(noticeId);
        return CommonResponse.onSuccess(null);
    }
}
