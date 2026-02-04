package com.lastcommit.piilot.domain.notification.service;

import com.lastcommit.piilot.domain.notification.dto.response.NotificationResponseDTO;
import com.lastcommit.piilot.domain.notification.dto.response.NotificationStatsResponseDTO;
import com.lastcommit.piilot.domain.notification.entity.Notification;
import com.lastcommit.piilot.domain.notification.exception.NotificationErrorStatus;
import com.lastcommit.piilot.domain.notification.repository.NotificationRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Slice<NotificationResponseDTO> getNotifications(Long userId, Pageable pageable) {
        Slice<Notification> notifications = notificationRepository
            .findByUserIdOrderByIssuedAtDesc(userId, pageable);
        return notifications.map(NotificationResponseDTO::from);
    }

    public List<NotificationResponseDTO> getRecentUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
            .findUnreadByUserIdOrderByIssuedAtDesc(userId, PageRequest.of(0, 3));
        return notifications.stream()
            .map(NotificationResponseDTO::from)
            .collect(Collectors.toList());
    }

    public NotificationStatsResponseDTO getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        return new NotificationStatsResponseDTO(count);
    }

    @Transactional
    public NotificationResponseDTO markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdWithUser(notificationId)
            .orElseThrow(() -> new GeneralException(NotificationErrorStatus.NOTIFICATION_NOT_FOUND));

        // 소유자 검증
        if (!notification.getUser().getId().equals(userId)) {
            throw new GeneralException(NotificationErrorStatus.NOTIFICATION_ACCESS_DENIED);
        }

        // 이미 읽음
        if (notification.getIsRead()) {
            return NotificationResponseDTO.from(notification);
        }

        notification.markAsRead(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        return NotificationResponseDTO.from(saved);
    }
}
