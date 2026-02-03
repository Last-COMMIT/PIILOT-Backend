package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilePiiRepository extends JpaRepository<FilePii, Long> {

    List<FilePii> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);

    List<FilePii> findByFileIdIn(List<Long> fileIds);
}
