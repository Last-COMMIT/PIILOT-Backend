package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilePiiIssueRepository extends JpaRepository<FilePiiIssue, Long> {

    Optional<FilePiiIssue> findByFileIdAndIssueStatus(Long fileId, IssueStatus issueStatus);

    void deleteByFileId(Long fileId);
}
