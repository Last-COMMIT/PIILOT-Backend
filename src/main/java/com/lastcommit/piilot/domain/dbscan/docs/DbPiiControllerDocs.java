package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiFilterOptionDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiSummaryResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

import java.util.List;

@Tag(name = "DB PII", description = "DB 개인정보 목록 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbPiiControllerDocs {

    @Operation(summary = "DB 개인정보 요약 통계 조회", description = "총 항목 수, 고위험 항목, 암호화율, 총 레코드 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<DbPiiSummaryResponseDTO> getSummary(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "DB 개인정보 목록 조회", description = "DB 개인정보 목록을 필터링하여 무한스크롤로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<DbPiiListResponseDTO>> getPiiList(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size,
            @Parameter(description = "DB 연결 ID 필터") Long connectionId,
            @Parameter(description = "테이블 ID 필터") Long tableId,
            @Parameter(description = "개인정보 유형 (NM, RRN, ADD, IP, PH, ACN, PP, EM, FACE)") String piiType,
            @Parameter(description = "암호화 여부 (true: 암호화됨, false: 비암호화)") Boolean encrypted,
            @Parameter(description = "위험도 수준 (HIGH, MEDIUM, LOW)") String riskLevel,
            @Parameter(description = "검색어 (연결명, 테이블명, 컬럼명)") String keyword
    );

    @Operation(summary = "DB 연결 목록 조회 (필터용)", description = "필터 드롭다운에 사용할 DB 연결 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<DbPiiFilterOptionDTO>> getConnectionOptions(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "테이블 목록 조회 (필터용)", description = "필터 드롭다운에 사용할 테이블 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<DbPiiFilterOptionDTO>> getTableOptions(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "DB 연결 ID (선택)") Long connectionId
    );
}
