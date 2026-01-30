package com.lastcommit.piilot.domain.dbscan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.ScanStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "db_scan_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbScanHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "db_server_connection_id", nullable = false)
    private DbServerConnection dbServerConnection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus status;

    @Column(name = "scan_start_time", nullable = false)
    private LocalDateTime scanStartTime;

    @Column(name = "scan_end_time")
    private LocalDateTime scanEndTime;

    @Column(name = "total_tables_count", nullable = false)
    private Long totalTablesCount;

    @Column(name = "total_columns_count", nullable = false)
    private Long totalColumnsCount;

    @Column(name = "scanned_columns_count", nullable = false)
    private Long scannedColumnsCount;

    @Builder
    private DbScanHistory(DbServerConnection dbServerConnection, ScanStatus status,
                          LocalDateTime scanStartTime, Long totalTablesCount,
                          Long totalColumnsCount, Long scannedColumnsCount) {
        this.dbServerConnection = dbServerConnection;
        this.status = status;
        this.scanStartTime = scanStartTime;
        this.totalTablesCount = totalTablesCount;
        this.totalColumnsCount = totalColumnsCount;
        this.scannedColumnsCount = scannedColumnsCount;
    }

    public void updateCompleted(LocalDateTime scanEndTime, ScanStatus status,
                               Long totalTablesCount, Long totalColumnsCount, Long scannedColumnsCount) {
        this.scanEndTime = scanEndTime;
        this.status = status;
        this.totalTablesCount = totalTablesCount;
        this.totalColumnsCount = totalColumnsCount;
        this.scannedColumnsCount = scannedColumnsCount;
    }
}
