package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.MaskingAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.MaskingAiResponseDTO;

public interface MaskingAiServerClient {

    MaskingAiResponseDTO maskFile(MaskingAiRequestDTO request);
}
