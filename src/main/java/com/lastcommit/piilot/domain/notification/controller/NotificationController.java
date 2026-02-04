package com.lastcommit.piilot.domain.notification.controller;

import com.lastcommit.piilot.domain.notification.docs.NotificationControllerDocs;
import com.lastcommit.piilot.domain.notification.dto.response.NotificationResponseDTO;
import com.lastcommit.piilot.domain.notification.dto.response.NotificationStatsResponseDTO;
import com.lastcommit.piilot.domain.notification.service.NotificationService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import com.lastcommit.piilot.global.validation.annotation.ValidPage;
import com.lastcommit.piilot.global.validation.annotation.ValidSize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController implements NotificationControllerDocs {

    private final NotificationService notificationService;

    @Override
    @GetMapping
    public CommonResponse<Slice<NotificationResponseDTO>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @ValidPage @RequestParam(defaultValue = "0") int page,
            @ValidSize @RequestParam(defaultValue = "10") int size
    ) {
        Slice<NotificationResponseDTO> result = notificationService
            .getNotifications(userId, PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/recent-unread")
    public CommonResponse<List<NotificationResponseDTO>> getRecentUnread(
            @AuthenticationPrincipal Long userId
    ) {
        List<NotificationResponseDTO> result = notificationService
            .getRecentUnreadNotifications(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/unread-count")
    public CommonResponse<NotificationStatsResponseDTO> getUnreadCount(
            @AuthenticationPrincipal Long userId
    ) {
        NotificationStatsResponseDTO result = notificationService.getUnreadCount(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PatchMapping("/{notificationId}/read")
    public CommonResponse<NotificationResponseDTO> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        NotificationResponseDTO result = notificationService
            .markAsRead(userId, notificationId);
        return CommonResponse.onSuccess(result);
    }
}
