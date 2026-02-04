package com.lastcommit.piilot.domain.notification.repository;

import com.lastcommit.piilot.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 모든 알림 조회 (최신순, 페이지네이션)
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "ORDER BY n.issuedAt DESC")
    Slice<Notification> findByUserIdOrderByIssuedAtDesc(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * 사용자의 읽지 않은 알림 조회 (최신순, 제한)
     * 헤더 드롭다운용 - 최대 3개
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId AND n.isRead = false " +
           "ORDER BY n.issuedAt DESC")
    List<Notification> findUnreadByUserIdOrderByIssuedAtDesc(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * 알림 ID로 조회 (소유자 검증용 - User 정보 포함)
     */
    @Query("SELECT n FROM Notification n JOIN FETCH n.user " +
           "WHERE n.id = :notificationId")
    Optional<Notification> findByIdWithUser(@Param("notificationId") Long notificationId);

    /**
     * 사용자의 읽지 않은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
}
