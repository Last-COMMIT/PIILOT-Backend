package com.lastcommit.piilot.domain.dbscan.dto.response;

import org.springframework.data.domain.Slice;

public record DbPiiIssueListResponseDTO(
        DbPiiIssueStatsResponseDTO stats,
        Slice<DbPiiIssueTableGroupResponseDTO> content
) {
    public static DbPiiIssueListResponseDTO of(
            DbPiiIssueStatsResponseDTO stats,
            Slice<DbPiiIssueTableGroupResponseDTO> content
    ) {
        return new DbPiiIssueListResponseDTO(stats, content);
    }
}
