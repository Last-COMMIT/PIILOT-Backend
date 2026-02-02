package com.lastcommit.piilot.domain.regulation.controller;

import com.lastcommit.piilot.domain.regulation.docs.RegulationSearchControllerDocs;
import com.lastcommit.piilot.domain.regulation.dto.request.RegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.RegulationSearchResponseDTO;
import com.lastcommit.piilot.domain.regulation.service.RegulationSearchService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/law-search")
@RequiredArgsConstructor
public class RegulationSearchController implements RegulationSearchControllerDocs {

    private final RegulationSearchService regulationSearchService;

    @Override
    @PostMapping
    public CommonResponse<RegulationSearchResponseDTO> search(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegulationSearchRequestDTO request
    ) {
        RegulationSearchResponseDTO result = regulationSearchService.search(userId, request);
        return CommonResponse.onSuccess(result);
    }
}
