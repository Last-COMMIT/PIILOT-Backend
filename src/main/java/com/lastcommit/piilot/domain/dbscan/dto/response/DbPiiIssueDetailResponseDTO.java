package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.domain.shared.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DbPiiIssueDetailResponseDTO(
        Long issueId,
        String connectionName,
        String dbmsTypeName,
        String tableName,
        String columnName,
        String piiTypeName,
        String piiTypeCode,
        Long totalRecordsCount,
        Long encRecordsCount,
        Long unencryptedCount,
        RiskLevel riskLevel,
        UserStatus userStatus,
        IssueStatus issueStatus,
        LocalDateTime detectedAt,
        String managerName,
        String managerEmail,
        List<UnencryptedRecordDTO> unencryptedRecords
) {
    public static DbPiiIssueDetailResponseDTO of(DbPiiIssue issue, List<UnencryptedRecordDTO> unencryptedRecords) {
        DbPiiColumn column = issue.getDbPiiColumn();
        DbTable table = column.getDbTable();
        DbServerConnection connection = table.getDbServerConnection();

        Long totalRecords = column.getTotalRecordsCount() != null ? column.getTotalRecordsCount() : 0L;
        Long encRecords = column.getEncRecordsCount() != null ? column.getEncRecordsCount() : 0L;
        Long unencCount = totalRecords - encRecords;

        return new DbPiiIssueDetailResponseDTO(
                issue.getId(),
                connection.getConnectionName(),
                connection.getDbmsType().getName(),
                table.getName(),
                column.getName(),
                column.getPiiType().getType().getDisplayName(),
                column.getPiiType().getType().name(),
                totalRecords,
                encRecords,
                unencCount,
                column.getRiskLevel(),
                issue.getUserStatus(),
                issue.getIssueStatus(),
                issue.getDetectedAt(),
                connection.getManagerName(),
                connection.getManagerEmail(),
                unencryptedRecords
        );
    }
}
