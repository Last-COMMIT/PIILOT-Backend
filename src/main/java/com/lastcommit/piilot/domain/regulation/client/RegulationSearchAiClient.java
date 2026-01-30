package com.lastcommit.piilot.domain.regulation.client;

import com.lastcommit.piilot.domain.regulation.dto.request.AiRegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.AiRegulationSearchResponseDTO;

public interface RegulationSearchAiClient {

    AiRegulationSearchResponseDTO search(AiRegulationSearchRequestDTO request);
}
