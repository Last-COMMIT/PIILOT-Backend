package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "masking_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaskingLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id")
    private FileServerConnection connection;

    @Column(name = "original_file_path", nullable = false, length = 1000)
    private String originalFilePath;

    @Column(name = "masked_file_path", nullable = false, length = 1000)
    private String maskedFilePath;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;



}
