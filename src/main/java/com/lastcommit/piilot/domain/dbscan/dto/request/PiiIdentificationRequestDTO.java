package com.lastcommit.piilot.domain.dbscan.dto.request;

import java.util.List;

public record PiiIdentificationRequestDTO(
        List<TableColumns> tables
) {
    public record TableColumns(
            String tableName,
            List<String> columns
    ) {
    }
}
