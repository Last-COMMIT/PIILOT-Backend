package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiIssueStatsDTO;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;

import java.util.List;

public interface FilePiiIssueCustomRepository {

    FilePiiIssueStatsDTO calculateStats(Long userId);

    List<FilePiiIssue> findActiveIssuesWithDetails(Long userId);
}
