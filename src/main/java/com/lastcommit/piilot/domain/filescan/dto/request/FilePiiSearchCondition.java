package com.lastcommit.piilot.domain.filescan.dto.request;

public record FilePiiSearchCondition(
        Long connectionId,
        String fileCategory,
        Boolean masked,
        String riskLevel,
        String keyword
) {
}
