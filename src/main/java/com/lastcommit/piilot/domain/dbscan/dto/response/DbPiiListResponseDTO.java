package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.shared.PiiCategory;

import java.time.LocalDateTime;

public record DbPiiListResponseDTO(
        Long id,
        String connectionName,
        String tableName,
        String columnName,
        String piiType,
        String piiTypeName,
        String encryptionStatus,
        Double encryptionRate,
        String riskLevel,
        LocalDateTime lastScannedAt
) {
    public static DbPiiListResponseDTO from(DbPiiColumn piiColumn) {
        String connectionName = piiColumn.getDbTable().getDbServerConnection().getConnectionName()
                + " (" + piiColumn.getDbTable().getDbServerConnection().getDbmsType().getName() + ")";

        PiiCategory category = piiColumn.getPiiType().getType();

        String encryptionStatus = determineEncryptionStatus(
                piiColumn.getEncRecordsCount(),
                piiColumn.getTotalRecordsCount()
        );

        Double encryptionRate = calculateEncryptionRate(
                piiColumn.getEncRecordsCount(),
                piiColumn.getTotalRecordsCount()
        );

        return new DbPiiListResponseDTO(
                piiColumn.getId(),
                connectionName,
                piiColumn.getDbTable().getName(),
                piiColumn.getName(),
                category.name(),
                category.getDisplayName(),
                encryptionStatus,
                encryptionRate,
                piiColumn.getRiskLevel() != null ? piiColumn.getRiskLevel().name() : null,
                piiColumn.getDbTable().getLastScannedAt()
        );
    }

    private static String determineEncryptionStatus(Long encRecordsCount, Long totalRecordsCount) {
        if (encRecordsCount == null || encRecordsCount == 0) {
            return "NOT_ENCRYPTED";
        } else if (totalRecordsCount != null && encRecordsCount.equals(totalRecordsCount)) {
            return "ENCRYPTED";
        } else {
            return "PARTIAL";
        }
    }

    private static Double calculateEncryptionRate(Long encRecordsCount, Long totalRecordsCount) {
        if (totalRecordsCount == null || totalRecordsCount == 0) {
            return 0.0;
        }
        if (encRecordsCount == null) {
            return 0.0;
        }
        return Math.round((encRecordsCount * 100.0 / totalRecordsCount) * 10) / 10.0;
    }
}
