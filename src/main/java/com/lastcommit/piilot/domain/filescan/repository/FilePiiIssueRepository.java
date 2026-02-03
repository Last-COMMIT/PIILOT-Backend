package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
