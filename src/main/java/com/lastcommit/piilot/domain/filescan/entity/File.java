package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private Long fileSize = 0L;

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

    @Column(name = "is_issue_open", nullable = false)
    private Boolean isIssueOpen = false;

    @Column(name = "total_issues_count", nullable = false)
    private Integer totalIssuesCount = 0;

    @Builder
    private File(FileServerConnection connection, FileType fileType, String name,
                 String filePath, Long fileSize, Boolean isEncrypted,
                 Boolean hasPersonalInfo, RiskLevel riskLevel,
                 LocalDateTime lastModifiedTime, LocalDateTime lastScannedAt) {
        this.connection = connection;
        this.fileType = fileType;
        this.name = name;
        this.filePath = filePath;
        this.fileSize = fileSize != null ? fileSize : 0L;
        this.isEncrypted = isEncrypted != null ? isEncrypted : false;
        this.hasPersonalInfo = hasPersonalInfo != null ? hasPersonalInfo : false;
        this.riskLevel = riskLevel;
        this.lastModifiedTime = lastModifiedTime;
        this.lastScannedAt = lastScannedAt;
        this.isIssueOpen = false;
        this.totalIssuesCount = 0;
    }

    public void updateScanResult(Boolean isEncrypted, Boolean hasPersonalInfo,
                                  RiskLevel riskLevel, LocalDateTime scannedAt) {
        this.isEncrypted = isEncrypted;
        this.hasPersonalInfo = hasPersonalInfo;
        this.riskLevel = riskLevel;
        this.lastScannedAt = scannedAt;
    }

    public void updateMetadata(Long fileSize, LocalDateTime lastModifiedTime) {
        this.fileSize = fileSize;
        this.lastModifiedTime = lastModifiedTime;
    }

    public void incrementIssueCount() {
        this.isIssueOpen = true;
        this.totalIssuesCount++;
    }

    public void closeIssue() {
        this.isIssueOpen = false;
    }

    public boolean needsRescan(LocalDateTime previousScanTime) {
        if (Boolean.TRUE.equals(this.isEncrypted)) {
            return false;
        }
        if (previousScanTime == null || this.lastScannedAt == null) {
            return true;
        }
        return this.lastModifiedTime.isAfter(this.lastScannedAt);
    }

    public void markAsEncrypted() {
        this.isEncrypted = true;
        this.isIssueOpen = false;
    }

    /**
     * 파일을 암호화된 ZIP으로 변환 시 경로와 타입을 업데이트합니다.
     *
     * @param zipFilePath ZIP 파일 경로
     * @param zipFileType ZIP 파일 타입 (ARCHIVE)
     */
    public void updateToEncryptedZip(String zipFilePath, FileType zipFileType) {
        this.filePath = zipFilePath;
        this.name = extractFileName(zipFilePath);
        this.fileType = zipFileType;
        this.isEncrypted = true;
        this.isIssueOpen = false;
    }

    private String extractFileName(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}