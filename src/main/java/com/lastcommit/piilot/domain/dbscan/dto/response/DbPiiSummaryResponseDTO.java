package com.lastcommit.piilot.domain.dbscan.dto.response;

public record DbPiiSummaryResponseDTO(
        long totalPiiColumns,
        long highRiskColumns,
        double encryptionRate,
        long totalRecords
) {
}
