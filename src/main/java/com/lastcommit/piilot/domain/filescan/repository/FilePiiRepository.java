package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilePiiRepository extends JpaRepository<FilePii, Long> {

    List<FilePii> findByFileId(Long fileId);

    @Query("SELECT COALESCE(SUM(fp.totalPiisCount), 0) FROM FilePii fp WHERE fp.file.id = :fileId")
    int sumTotalPiisCountByFileId(@Param("fileId") Long fileId);

    @Query("SELECT COALESCE(SUM(fp.maskedPiisCount), 0) FROM FilePii fp WHERE fp.file.id = :fileId")
    int sumMaskedPiisCountByFileId(@Param("fileId") Long fileId);

    @Query("SELECT COALESCE(SUM(fp.totalPiisCount), 0) FROM FilePii fp " +
            "JOIN fp.file f " +
            "JOIN f.connection c " +
            "WHERE c.user.id = :userId")
    long sumTotalPiisCountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(fp.maskedPiisCount), 0) FROM FilePii fp " +
            "JOIN fp.file f " +
            "JOIN f.connection c " +
            "WHERE c.user.id = :userId")
    long sumMaskedPiisCountByUserId(@Param("userId") Long userId);
}
