package com.lastcommit.piilot.domain.dbscan.dto.response;

import java.util.List;

public record EncryptionCheckResponseDTO(
        List<EncryptionResult> results
) {
    public record EncryptionResult(
            String tableName,
            String columnName,
            String piiType,
            Long totalRecordsCount,
            Long encRecordsCount,
            List<Long> unencRecordsKeys
    ) {
    }
}
