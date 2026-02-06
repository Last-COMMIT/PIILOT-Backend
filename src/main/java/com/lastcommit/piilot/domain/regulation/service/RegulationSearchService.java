package com.lastcommit.piilot.domain.regulation.service;

import com.lastcommit.piilot.domain.regulation.client.RegulationSearchAiClient;
import com.lastcommit.piilot.domain.regulation.dto.request.AiRegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.request.RegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.AiRegulationSearchResponseDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.RegulationSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegulationSearchService {

    private final RegulationSearchAiClient aiClient;

    public RegulationSearchResponseDTO search(Long userId, RegulationSearchRequestDTO request) {
        log.info("법령/내규 검색 요청: userId={}, query={}", userId, request.query());

        AiRegulationSearchRequestDTO aiRequest = AiRegulationSearchRequestDTO.of(request.query());
        AiRegulationSearchResponseDTO aiResponse = aiClient.search(aiRequest);

        RegulationSearchResponseDTO response = RegulationSearchResponseDTO.from(aiResponse);

        log.info("법령/내규 검색 완료: userId={}, totalReferences={}", userId, response.totalReferences());
        return response;
    }
}
