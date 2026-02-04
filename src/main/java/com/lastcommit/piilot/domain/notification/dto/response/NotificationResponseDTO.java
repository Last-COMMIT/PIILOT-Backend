package com.lastcommit.piilot.domain.notification.dto.response;

import com.lastcommit.piilot.domain.notification.entity.Notification;
import com.lastcommit.piilot.domain.shared.EntityType;
import com.lastcommit.piilot.domain.shared.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
    Long id,
    NotificationType type,
    String title,
    String message,
    EntityType entityType,
    Long entityId,
    Boolean isRead,
    LocalDateTime issuedAt,
    LocalDateTime readAt
) {
    public static NotificationResponseDTO from(Notification notification) {
        return new NotificationResponseDTO(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getEntityType(),
            notification.getEntityId(),
            notification.getIsRead(),
            notification.getIssuedAt(),
            notification.getReadAt()
        );
    }
}
