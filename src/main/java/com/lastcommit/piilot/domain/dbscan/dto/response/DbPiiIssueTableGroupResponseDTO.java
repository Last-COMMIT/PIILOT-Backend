package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;

import java.util.List;

public record DbPiiIssueTableGroupResponseDTO(
        Long tableId,
        String tableName,
        String connectionName,
        String dbmsTypeName,
        Integer issueCount,
        List<DbPiiIssueResponseDTO> issues
) {
    public static DbPiiIssueTableGroupResponseDTO of(DbTable table, List<DbPiiIssue> issues) {
        List<DbPiiIssueResponseDTO> issueDTOs = issues.stream()
                .map(DbPiiIssueResponseDTO::from)
                .toList();

        return new DbPiiIssueTableGroupResponseDTO(
                table.getId(),
                table.getName(),
                table.getDbServerConnection().getConnectionName(),
                table.getDbServerConnection().getDbmsType().getName(),
                issues.size(),
                issueDTOs
        );
    }
}
