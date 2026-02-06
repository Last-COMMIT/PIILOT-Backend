package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;

import java.util.List;

public record FilePiiIssueServerGroupResponseDTO(
        Long connectionId,
        String connectionName,
        String serverTypeName,
        String managerName,
        Integer issueCount,
        List<FilePiiIssueResponseDTO> issues
) {
    public static FilePiiIssueServerGroupResponseDTO of(
            FileServerConnection connection,
            List<FilePiiIssueResponseDTO> issues
    ) {
        return new FilePiiIssueServerGroupResponseDTO(
                connection.getId(),
                connection.getConnectionName(),
                connection.getServerType().getName(),
                connection.getManagerName(),
                issues.size(),
                issues
        );
    }
}
