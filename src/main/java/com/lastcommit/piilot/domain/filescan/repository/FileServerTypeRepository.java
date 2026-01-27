package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FileServerType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerTypeRepository extends JpaRepository<FileServerType, Integer> {
}
