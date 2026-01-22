package com.lastcommit.piilot.domain.filescan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileCategory type;

    @Column(nullable = false, length = 30)
    private String extension;
}
