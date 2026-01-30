package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiIssueStatsDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;

import java.util.List;

public interface DbPiiIssueCustomRepository {

    DbPiiIssueStatsDTO calculateStats(Long userId);

    List<DbPiiIssue> findActiveIssuesWithDetails(Long userId);
}
