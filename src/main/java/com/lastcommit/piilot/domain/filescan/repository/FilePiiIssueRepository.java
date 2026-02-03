package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilePiiIssueRepository extends JpaRepository<FilePiiIssue, Long>, FilePiiIssueCustomRepository {

    Optional<FilePiiIssue> findByFileIdAndIssueStatus(Long fileId, IssueStatus issueStatus);

    void deleteByFileId(Long fileId);

    @Query("SELECT i FROM FilePiiIssue i " +
            "JOIN FETCH i.file f " +
            "JOIN FETCH i.connection c " +
            "JOIN FETCH c.serverType " +
            "JOIN FETCH f.fileType " +
            "WHERE i.id = :issueId")
    Optional<FilePiiIssue> findByIdWithDetails(@Param("issueId") Long issueId);

    // Dashboard: 사용자의 활성 이슈 수
    @Query("SELECT COUNT(i) FROM FilePiiIssue i " +
            "WHERE i.connection.user.id = :userId " +
            "AND i.issueStatus = :status")
    long countByUserIdAndIssueStatus(@Param("userId") Long userId, @Param("status") IssueStatus status);

    // Dashboard: 최근 이슈 4개 조회 (fetch join)
    @Query("SELECT i FROM FilePiiIssue i " +
            "JOIN FETCH i.file f " +
            "JOIN FETCH i.connection c " +
            "WHERE i.connection.user.id = :userId " +
            "AND i.issueStatus = :status " +
            "ORDER BY i.detectedAt DESC " +
            "LIMIT 4")
    List<FilePiiIssue> findTop4ByUserIdAndStatusOrderByDetectedAtDesc(
            @Param("userId") Long userId, @Param("status") IssueStatus status);

    // Dashboard: 최근 12개월 이슈 발생 건수 (detected_at 기준)
    @Query("SELECT CONCAT(EXTRACT(YEAR FROM i.detectedAt), '-', " +
            "LPAD(CAST(EXTRACT(MONTH FROM i.detectedAt) AS text), 2, '0')) as yearMonth, " +
            "COUNT(i) FROM FilePiiIssue i " +
            "WHERE i.connection.user.id = :userId " +
            "AND i.detectedAt >= :startDate " +
            "GROUP BY EXTRACT(YEAR FROM i.detectedAt), EXTRACT(MONTH FROM i.detectedAt) " +
            "ORDER BY yearMonth")
    List<Object[]> countByLast12Months(@Param("userId") Long userId, @Param("startDate") java.time.LocalDateTime startDate);
}
