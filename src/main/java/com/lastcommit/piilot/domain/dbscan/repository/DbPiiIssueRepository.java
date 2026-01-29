package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbPiiIssueRepository extends JpaRepository<DbPiiIssue, Long>, DbPiiIssueRepositoryCustom {
}
