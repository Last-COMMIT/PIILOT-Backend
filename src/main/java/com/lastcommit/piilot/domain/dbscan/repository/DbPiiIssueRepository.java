package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DbPiiIssueRepository extends JpaRepository<DbPiiIssue, Long>, DbPiiIssueRepositoryCustom {

    Optional<DbPiiIssue> findByDbPiiColumnIdAndIssueStatus(Long columnId, IssueStatus issueStatus);
}
