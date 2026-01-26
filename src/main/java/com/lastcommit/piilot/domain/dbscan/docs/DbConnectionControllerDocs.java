package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbConnectionRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "DB Connection", description = "DB 서버 연결 관리 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbConnectionControllerDocs {

    @Operation(summary = "DB 연결 생성", description = "새로운 DB 서버 연결을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "연결 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "DBMS 유형 없음"),
            @ApiResponse(responseCode = "409", description = "연결 이름 중복")
    })
    CommonResponse<DbConnectionResponseDTO> createConnection(
            @Parameter(hidden = true) Long userId,
            DbConnectionRequestDTO request
    );

    @Operation(summary = "DB 연결 수정", description = "기존 DB 서버 연결 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "연결 정보 없음"),
            @ApiResponse(responseCode = "409", description = "연결 이름 중복")
    })
    CommonResponse<DbConnectionResponseDTO> updateConnection(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID") Long connectionId,
            DbConnectionRequestDTO request
    );
}
