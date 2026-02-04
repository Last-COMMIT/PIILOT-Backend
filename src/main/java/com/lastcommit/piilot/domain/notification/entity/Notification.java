package com.lastcommit.piilot.domain.notification.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.EntityType;
import com.lastcommit.piilot.domain.shared.NotificationType;
import com.lastcommit.piilot.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 50)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Builder
    private Notification(User user, NotificationType type, String title,
                        String message, EntityType entityType, Long entityId,
                        Boolean isRead, LocalDateTime issuedAt) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.isRead = isRead != null ? isRead : false;
        this.issuedAt = issuedAt != null ? issuedAt : LocalDateTime.now();
    }

    public void markAsRead(LocalDateTime readAt) {
        this.isRead = true;
        this.readAt = readAt;
    }
}
