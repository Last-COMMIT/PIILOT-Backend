package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_pii_issues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilePiiIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id")
    private FileServerConnection connection;

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

    @Builder
    private FilePiiIssue(File file, FileServerConnection connection,
                         UserStatus userStatus, IssueStatus issueStatus,
                         LocalDateTime detectedAt) {
        this.file = file;
        this.connection = connection;
        this.userStatus = userStatus;
        this.issueStatus = issueStatus;
        this.detectedAt = detectedAt;
    }

    public void resolve(LocalDateTime resolvedAt) {
        this.issueStatus = IssueStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
    }

    public void updateUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
