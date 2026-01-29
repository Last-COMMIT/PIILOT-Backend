package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.request.EncryptionCheckRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.PiiIdentificationRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;

public interface AiServerClient {

    PiiIdentificationResponseDTO identifyPiiColumns(PiiIdentificationRequestDTO request);

    EncryptionCheckResponseDTO checkEncryption(EncryptionCheckRequestDTO request);
}
