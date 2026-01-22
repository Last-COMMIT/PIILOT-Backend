package com.lastcommit.piilot.domain.dbscan.entity;


import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.ScanStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
