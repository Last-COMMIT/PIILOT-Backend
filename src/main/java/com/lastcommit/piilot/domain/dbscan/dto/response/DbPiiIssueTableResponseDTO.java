package com.lastcommit.piilot.domain.dbscan.dto.response;

import java.util.List;

public record DbPiiIssueTableResponseDTO(
        Long tableId,
        String tableName,
        String connectionInfo,      // "연결명 (DBMS타입) | 담당자: 담당자명"
        int issueCount,
        List<DbPiiIssueItemDTO> issues
) {}
