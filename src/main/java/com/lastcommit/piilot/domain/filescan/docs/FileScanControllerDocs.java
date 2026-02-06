package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.response.FileScanResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanStatusResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "File Scan", description = "파일 서버 스캔 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface FileScanControllerDocs {

    @Operation(summary = "파일 서버 수동 스캔", description = "지정한 파일 서버에 대해 스캔을 시작합니다. 스캔은 비동기로 실행됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "스캔 시작됨"),
            @ApiResponse(responseCode = "400", description = "연결되지 않은 서버"),
            @ApiResponse(responseCode = "404", description = "연결 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 스캔 진행 중")
    })
    CommonResponse<FileScanResponseDTO> startScan(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID") Long connectionId
    );

    @Operation(summary = "스캔 상태 조회", description = "진행 중인 스캔의 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "스캔 이력 없음")
    })
    CommonResponse<FileScanStatusResponseDTO> getScanStatus(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID") Long connectionId,
            @Parameter(description = "스캔 이력 ID") Long scanHistoryId
    );
}
