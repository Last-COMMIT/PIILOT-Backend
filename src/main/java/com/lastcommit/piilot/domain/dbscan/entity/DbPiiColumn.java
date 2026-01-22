package com.lastcommit.piilot.domain.dbscan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.PiiType;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "db_pii_columns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbPiiColumn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private DbTable dbTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pii_type_id", nullable = false)
    private PiiType piiType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "total_records_count")
    private Long totalRecordsCount;

    @Column(name = "enc_records_count")
    private Long encRecordsCount;

    //비암호화된 레코드의 ID 값들을 저장하는 컬럼 - JSON배열 [1,5,23,156]형식
    @Column(name = "unenc_records_key", columnDefinition = "TEXT")
    private String unencRecordsKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel;

    @Column(name = "total_issues_count")
    private Integer totalIssuesCount;

    @Column(name = "is_issue_open", nullable = false)
    private Boolean isIssueOpen;
}
