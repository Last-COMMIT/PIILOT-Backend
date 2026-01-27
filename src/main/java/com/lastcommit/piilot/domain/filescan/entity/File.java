package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private FileServerConnection connection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_id", nullable = false)
    private FileType fileType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    @Column(name = "has_personal_info", nullable = false)
    private Boolean hasPersonalInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel;

    @Column(name = "last_modified_time", nullable = false)
    private LocalDateTime lastModifiedTime;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;
}