package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilePiiRepository extends JpaRepository<FilePii, Long> {

    List<FilePii> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);

    // Cascade delete: 특정 connection의 모든 FilePii 삭제
    void deleteByFileConnectionId(Long connectionId);

    @Query("SELECT fp FROM FilePii fp JOIN FETCH fp.piiType WHERE fp.file.id = :fileId")
    List<FilePii> findByFileIdWithPiiType(@Param("fileId") Long fileId);

    List<FilePii> findByFileIdIn(List<Long> fileIds);

    // Dashboard: PII 유형별 분포 (파일)
    @Query("SELECT fp.piiType.type, COALESCE(SUM(fp.totalPiisCount), 0) FROM FilePii fp " +
            "WHERE fp.file.connection.user.id = :userId " +
            "GROUP BY fp.piiType.type")
    List<Object[]> getPiiDistributionByUserId(@Param("userId") Long userId);
}
