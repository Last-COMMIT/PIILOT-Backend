package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileTypeRepository extends JpaRepository<FileType, Integer> {

    Optional<FileType> findByExtension(String extension);
}
