package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiColumnListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiConnectionResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiTableResponseDTO;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import com.lastcommit.piilot.global.validation.annotation.ValidPage;
import com.lastcommit.piilot.global.validation.annotation.ValidSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "DB PII", description = "DB 개인정보 목록 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbPiiControllerDocs {

    @Operation(summary = "커넥션 목록 조회", description = "필터 드롭다운용 DB 커넥션 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<DbPiiConnectionResponseDTO>> getConnections(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "테이블 목록 조회", description = "선택한 커넥션의 테이블 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "연결 정보 없음")
    })
    CommonResponse<List<DbPiiTableResponseDTO>> getTables(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "커넥션 ID") Long connectionId
    );

    @Operation(summary = "PII 컬럼 목록 조회", description = "필터링 및 검색 조건에 따른 DB 개인정보 컬럼 목록과 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "연결/테이블 정보 없음")
    })
    CommonResponse<DbPiiColumnListResponseDTO> getPiiColumns(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "커넥션 ID (선택)") Long connectionId,
            @Parameter(description = "테이블 ID (선택)") Long tableId,
            @Parameter(description = "PII 유형 (선택): NM, EM, PH, RRN, ADD, IP, ACN, PP") PiiCategory piiType,
            @Parameter(description = "암호화 여부 (선택): true=암호화됨, false=보안필요") Boolean encrypted,
            @Parameter(description = "위험도 (선택): HIGH, MEDIUM, LOW") RiskLevel riskLevel,
            @Parameter(description = "컬럼명 검색어 (선택)") String keyword,
            @ValidPage @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @ValidSize @Parameter(description = "페이지 크기") int size
    );
}
