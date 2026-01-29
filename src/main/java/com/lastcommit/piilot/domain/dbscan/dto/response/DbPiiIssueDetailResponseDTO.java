package com.lastcommit.piilot.domain.dbscan.dto.response;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;

import java.time.LocalDateTime;
import java.util.List;

public record DbPiiIssueDetailResponseDTO(
        Long issueId,
        String warningMessage,

        // 암호화되지 않은 데이터 샘플 (최대 10개)
        List<UnencryptedSampleDTO> unencryptedSamples,

        // 이슈 상세 정보
        String connectionName,
        String tableName,
        String columnName,
        String piiType,
        String piiTypeName,
        String riskLevel,
        Long unencryptedCount,      // 보안필요 레코드 수
        Long totalRecords,          // 총 레코드 수
        LocalDateTime lastScannedAt,
        String managerName,
        String managerEmail,
        String userStatus,
        LocalDateTime detectedAt
) {
    private static final String WARNING_MESSAGE =
            "이 컬럼의 개인정보는 평문으로 저장되어 있어 보안 위험이 있습니다. 즉시 암호화 조치가 필요합니다.";

    public static DbPiiIssueDetailResponseDTO from(DbPiiIssue issue, List<UnencryptedSampleDTO> samples) {
        DbPiiColumn column = issue.getDbPiiColumn();
        DbTable table = column.getDbTable();
        DbServerConnection connection = table.getDbServerConnection();

        long totalRecords = column.getTotalRecordsCount() != null ? column.getTotalRecordsCount() : 0L;
        long encRecords = column.getEncRecordsCount() != null ? column.getEncRecordsCount() : 0L;
        long unencryptedCount = totalRecords - encRecords;

        return new DbPiiIssueDetailResponseDTO(
                issue.getId(),
                WARNING_MESSAGE,
                samples,
                connection.getConnectionName() + " (" + connection.getDbmsType().getName() + ")",
                table.getName(),
                column.getName(),
                column.getPiiType().getType().name(),
                column.getPiiType().getType().getDisplayName(),
                column.getRiskLevel().name(),
                unencryptedCount,
                totalRecords,
                table.getLastScannedAt(),
                connection.getManagerName(),
                connection.getManagerEmail(),
                issue.getUserStatus().name(),
                issue.getDetectedAt()
        );
    }
}
