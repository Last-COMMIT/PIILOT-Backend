package com.lastcommit.piilot.domain.filescan.dto.response;

import org.springframework.data.domain.Slice;

public record FilePiiIssueListResponseDTO(
        FilePiiIssueStatsResponseDTO stats,
        Slice<FilePiiIssueServerGroupResponseDTO> content
) {
    public static FilePiiIssueListResponseDTO of(
            FilePiiIssueStatsResponseDTO stats,
            Slice<FilePiiIssueServerGroupResponseDTO> content
    ) {
        return new FilePiiIssueListResponseDTO(stats, content);
    }
}
