package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.response.DbScanResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "DB Scan", description = "DB 스캔 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbScanControllerDocs {

    @Operation(summary = "DB 수동 스캔", description = "지정한 DB 연결에 대해 전체 스캔을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스캔 완료"),
            @ApiResponse(responseCode = "400", description = "연결되지 않은 DB"),
            @ApiResponse(responseCode = "404", description = "연결 정보 없음"),
            @ApiResponse(responseCode = "500", description = "스캔 실패")
    })
    CommonResponse<DbScanResponseDTO> scanConnection(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID") Long connectionId
    );
}
