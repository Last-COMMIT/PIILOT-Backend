package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueTableResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface DbPiiIssueRepositoryCustom {

    /**
     * 이슈 요약 통계 조회
     */
    DbPiiIssueSummaryResponseDTO getSummary(Long userId);

    /**
     * 테이블별 이슈 목록 조회 (무한스크롤)
     */
    Slice<DbPiiIssueTableResponseDTO> getIssuesByTable(Long userId, Pageable pageable);

    /**
     * 이슈 상세 조회 (권한 검증 포함)
     */
    Optional<DbPiiIssue> findByIdWithDetails(Long issueId, Long userId);
}
