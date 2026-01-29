package com.lastcommit.piilot.domain.dbscan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "db_pii_issues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbPiiIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private DbPiiColumn dbPiiColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "db_server_connection_id")
    private DbServerConnection dbServerConnection;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_status", nullable = false, length = 20)
    private IssueStatus issueStatus;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 작업 상태 변경
     */
    public void updateUserStatus(UserStatus newStatus) {
        this.userStatus = newStatus;
    }

    /**
     * 다음 작업 상태로 전환 (순환: ISSUE -> RUNNING -> DONE -> ISSUE)
     */
    public void cycleUserStatus() {
        this.userStatus = this.userStatus.next();
    }
}
