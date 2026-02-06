package com.lastcommit.piilot.domain.dbscan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dbms_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbmsType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "jdbc_prefix", nullable = false) // length = 255 는 기본값
    private String jdbcPrefix;

    @Column(name = "default_port", nullable = false)
    private Integer defaultPort;

    @Column(name = "driver_class_name", nullable = false)
    private String driverClassName;

    @Column(name = "test_query", nullable = false)
    private String testQuery;
}
