package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.PiiType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_pii")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilePii extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private PiiType piiType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(name = "total_piis_count")
    private Integer totalPiisCount;

    @Column(name = "masked_piis_count")
    private Integer maskedPiisCount;

    @Builder
    private FilePii(PiiType piiType, File file, Integer totalPiisCount, Integer maskedPiisCount) {
        this.piiType = piiType;
        this.file = file;
        this.totalPiisCount = totalPiisCount != null ? totalPiisCount : 0;
        this.maskedPiisCount = maskedPiisCount != null ? maskedPiisCount : 0;
    }

    public void updatePiiCounts(Integer totalPiisCount, Integer maskedPiisCount) {
        this.totalPiisCount = totalPiisCount;
        this.maskedPiisCount = maskedPiisCount;
    }

    public void markAllAsMasked() {
        this.maskedPiisCount = this.totalPiisCount;
    }
}
