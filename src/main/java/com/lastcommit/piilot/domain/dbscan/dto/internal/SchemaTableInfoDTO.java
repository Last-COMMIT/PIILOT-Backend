package com.lastcommit.piilot.domain.dbscan.dto.internal;

import java.util.List;

public record SchemaTableInfoDTO(
        String tableName,
        List<String> columns,
        long columnCount,
        String changeSignature
) {
}
