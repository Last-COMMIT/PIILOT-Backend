package com.lastcommit.piilot.domain.regulation.docs;

import com.lastcommit.piilot.domain.regulation.dto.request.RegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.RegulationSearchResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Regulation Search", description = "법령/내규 검색 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface RegulationSearchControllerDocs {

    @Operation(summary = "법령/내규 검색", description = "자연어 기반으로 개인정보 관련 법령 및 내규를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (검색어 누락 또는 길이 초과)"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    CommonResponse<RegulationSearchResponseDTO> search(
            @Parameter(hidden = true) Long userId,
            RegulationSearchRequestDTO request
    );
}
