package com.lastcommit.piilot.domain.dashboard.dto.response;

public record PiiDistributionDTO(
        String piiType,
        String piiTypeName,
        Long count,
        Double percentage
) {
    public static PiiDistributionDTO of(String piiType, String piiTypeName, Long count, Long totalCount) {
        double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
        return new PiiDistributionDTO(
                piiType,
                piiTypeName,
                count,
                Math.round(percentage * 10.0) / 10.0
        );
    }
}
