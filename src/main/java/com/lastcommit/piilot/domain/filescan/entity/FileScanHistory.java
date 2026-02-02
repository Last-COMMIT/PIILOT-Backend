package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.ScanStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_scan_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileScanHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_server_connection_id", nullable = false)
    private FileServerConnection fileServerConnection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus status;

    @Column(name = "scan_start_time", nullable = false)
    private LocalDateTime scanStartTime;

    @Column(name = "scan_end_time")
    private LocalDateTime scanEndTime;

    @Column(name = "total_files_count", nullable = false)
    private Long totalFilesCount;

    @Column(name = "total_files_size", nullable = false)
    private Long totalFilesSize;

    @Column(name = "scanned_files_count", nullable = false)
    private Long scannedFilesCount;

    @Builder
    private FileScanHistory(FileServerConnection fileServerConnection, ScanStatus status,
                            LocalDateTime scanStartTime, Long totalFilesCount,
                            Long totalFilesSize, Long scannedFilesCount) {
        this.fileServerConnection = fileServerConnection;
        this.status = status;
        this.scanStartTime = scanStartTime;
        this.totalFilesCount = totalFilesCount != null ? totalFilesCount : 0L;
        this.totalFilesSize = totalFilesSize != null ? totalFilesSize : 0L;
        this.scannedFilesCount = scannedFilesCount != null ? scannedFilesCount : 0L;
    }

    public void updateCompleted(LocalDateTime scanEndTime, Long totalFilesCount,
                                 Long totalFilesSize, Long scannedFilesCount) {
        this.status = ScanStatus.COMPLETED;
        this.scanEndTime = scanEndTime;
        this.totalFilesCount = totalFilesCount;
        this.totalFilesSize = totalFilesSize;
        this.scannedFilesCount = scannedFilesCount;
    }

    public void updateFailed(LocalDateTime scanEndTime) {
        this.status = ScanStatus.FAILED;
        this.scanEndTime = scanEndTime;
    }
}