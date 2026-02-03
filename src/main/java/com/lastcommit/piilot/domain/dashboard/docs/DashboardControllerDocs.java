package com.lastcommit.piilot.domain.dashboard.docs;

import com.lastcommit.piilot.domain.dashboard.dto.response.DashboardSummaryResponseDTO;
import com.lastcommit.piilot.domain.dashboard.dto.response.DashboardTrendResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "대시보드 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DashboardControllerDocs {

    @Operation(
            summary = "대시보드 요약 조회",
            description = "통계, 개인정보 유형별 분포, 최근 이슈를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<DashboardSummaryResponseDTO> getSummary(
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "대시보드 추세 조회",
            description = "최근 12개월 이슈 발생 추세를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<DashboardTrendResponseDTO> getTrends(
            @Parameter(hidden = true) Long userId
    );
}
