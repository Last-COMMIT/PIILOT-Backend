package com.lastcommit.piilot.domain.dbscan.dto.response;

public record DbPiiIssueSummaryResponseDTO(
        long totalIssueColumns,    // 총 이슈 컬럼 수
        long highRiskCount,        // 위험도 높음
        long mediumRiskCount,      // 위험도 중간
        long lowRiskCount,         // 위험도 낮음
        long totalRecords          // 총 개인정보 수
) {}
