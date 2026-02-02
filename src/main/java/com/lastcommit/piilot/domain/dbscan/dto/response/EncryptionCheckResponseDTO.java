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
            List<String> unencRecordsKeys  // String으로 변경하여 UUID/문자열 PK 지원
    ) {
    }
}
