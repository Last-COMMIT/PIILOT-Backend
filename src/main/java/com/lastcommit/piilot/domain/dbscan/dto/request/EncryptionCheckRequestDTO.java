package com.lastcommit.piilot.domain.dbscan.dto.request;

import java.util.List;

public record EncryptionCheckRequestDTO(
        Long connectionId,
        List<PiiColumnInfo> piiColumns
) {
    public record PiiColumnInfo(
            String tableName,
            String columnName,
            String piiType
    ) {
    }
}
