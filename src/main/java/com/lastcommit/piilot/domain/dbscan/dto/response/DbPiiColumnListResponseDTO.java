package com.lastcommit.piilot.domain.dbscan.dto.response;

import org.springframework.data.domain.Slice;

public record DbPiiColumnListResponseDTO(
        DbPiiStatsResponseDTO stats,
        Slice<DbPiiColumnResponseDTO> content
) {
    public static DbPiiColumnListResponseDTO of(DbPiiStatsResponseDTO stats, Slice<DbPiiColumnResponseDTO> content) {
        return new DbPiiColumnListResponseDTO(stats, content);
    }
}
