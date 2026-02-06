package com.lastcommit.piilot.domain.dbscan.dto.internal;

import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;

import java.util.List;

public record TableScanResultDTO(
        String tableName,
        List<PiiIdentificationResponseDTO.PiiColumnResult> piiColumns,
        List<EncryptionCheckResponseDTO.EncryptionResult> encryptionResults
) {
}
