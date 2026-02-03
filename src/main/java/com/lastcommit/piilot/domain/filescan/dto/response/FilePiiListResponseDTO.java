package com.lastcommit.piilot.domain.filescan.dto.response;

import org.springframework.data.domain.Slice;

public record FilePiiListResponseDTO(
        FilePiiStatsResponseDTO stats,
        Slice<FilePiiResponseDTO> content
) {
    public static FilePiiListResponseDTO of(FilePiiStatsResponseDTO stats, Slice<FilePiiResponseDTO> content) {
        return new FilePiiListResponseDTO(stats, content);
    }
}
