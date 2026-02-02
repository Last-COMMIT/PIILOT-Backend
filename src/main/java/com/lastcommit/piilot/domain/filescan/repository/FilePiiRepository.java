package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilePiiRepository extends JpaRepository<FilePii, Long> {

    List<FilePii> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);

    @Query("SELECT fp FROM FilePii fp JOIN FETCH fp.piiType WHERE fp.file.id = :fileId")
    List<FilePii> findByFileIdWithPiiType(@Param("fileId") Long fileId);
}
