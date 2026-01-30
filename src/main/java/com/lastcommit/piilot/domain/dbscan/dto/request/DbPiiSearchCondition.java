package com.lastcommit.piilot.domain.dbscan.dto.request;

public record DbPiiSearchCondition(
        Long connectionId,
        Long tableId,
        String piiType,
        Boolean encrypted,
        String riskLevel,
        String keyword
) {
}
