package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;

public record FilePiiConnectionResponseDTO(
        Long id,
        String connectionName,
        String serverTypeName
) {
    public static FilePiiConnectionResponseDTO from(FileServerConnection connection) {
        return new FilePiiConnectionResponseDTO(
                connection.getId(),
                connection.getConnectionName(),
                connection.getServerType().getName()
        );
    }
}
