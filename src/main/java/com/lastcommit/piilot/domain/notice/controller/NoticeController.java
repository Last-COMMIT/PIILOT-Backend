package com.lastcommit.piilot.domain.notice.controller;

import com.lastcommit.piilot.domain.notice.docs.NoticeControllerDocs;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeDetailResponseDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeListResponseDTO;
import com.lastcommit.piilot.domain.notice.service.NoticeService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController implements NoticeControllerDocs {

    private final NoticeService noticeService;

    @Override
    @GetMapping
    public CommonResponse<Slice<NoticeListResponseDTO>> getNoticeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<NoticeListResponseDTO> result = noticeService.getNoticeList(PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/{noticeId}")
    public CommonResponse<NoticeDetailResponseDTO> getNoticeDetail(
            @PathVariable Long noticeId
    ) {
        NoticeDetailResponseDTO result = noticeService.getNoticeDetail(noticeId);
        return CommonResponse.onSuccess(result);
    }
}
