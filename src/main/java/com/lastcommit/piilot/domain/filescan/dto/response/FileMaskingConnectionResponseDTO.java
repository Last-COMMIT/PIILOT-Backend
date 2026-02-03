package com.lastcommit.piilot.domain.filescan.dto.response;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;

public record FileMaskingConnectionResponseDTO(
        Long connectionId,
        String connectionName
) {
    public static FileMaskingConnectionResponseDTO from(FileServerConnection connection) {
        return new FileMaskingConnectionResponseDTO(
                connection.getId(),
                connection.getConnectionName()
        );
    }
}
