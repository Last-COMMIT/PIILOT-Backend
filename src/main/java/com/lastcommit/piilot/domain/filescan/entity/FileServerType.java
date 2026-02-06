package com.lastcommit.piilot.domain.filescan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_server_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;
}
