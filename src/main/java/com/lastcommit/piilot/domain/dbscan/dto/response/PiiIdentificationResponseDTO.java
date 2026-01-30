package com.lastcommit.piilot.domain.dbscan.dto.response;

import java.util.List;

public record PiiIdentificationResponseDTO(
        List<PiiColumnResult> piiColumns
) {
    public record PiiColumnResult(
            String tableName,
            String columnName,
            String piiType
    ) {
    }
}
