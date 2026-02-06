package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;

import java.time.LocalDateTime;

public record DbPiiColumnResponseDTO(
        Long id,
        String connectionName,
        String dbmsTypeName,
        String tableName,
        String columnName,
        String piiTypeName,
        PiiCategory piiTypeCode,
        Boolean encrypted,
        RiskLevel riskLevel,
        LocalDateTime lastScannedAt
) {
    public static DbPiiColumnResponseDTO from(DbPiiColumn column) {
        var table = column.getDbTable();
        var connection = table.getDbServerConnection();
        var piiType = column.getPiiType();

        boolean isEncrypted = column.getEncRecordsCount() != null
                && column.getTotalRecordsCount() != null
                && column.getEncRecordsCount().equals(column.getTotalRecordsCount());

        return new DbPiiColumnResponseDTO(
                column.getId(),
                connection.getConnectionName(),
                connection.getDbmsType().getName(),
                table.getName(),
                column.getName(),
                piiType.getType().getDisplayName(),
                piiType.getType(),
                isEncrypted,
                column.getRiskLevel(),
                table.getLastScannedAt()
        );
    }
}
