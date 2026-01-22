package com.lastcommit.piilot.domain.dbscan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "db_tables")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "db_server_connection_id", nullable = false)
    private DbServerConnection dbServerConnection;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "total_columns", nullable = false)
    private Long totalColumns;

    @Column(name = "last_change_signature", length = 255)
    private String lastChangeSignature;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

}
