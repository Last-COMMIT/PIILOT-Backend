package com.lastcommit.piilot.domain.shared;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pii_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PiiType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PiiCategory type;

    @Column(name = "risk_weight", nullable = false)
    private Float riskWeight;
}
