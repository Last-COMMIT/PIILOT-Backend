package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiIssueStatusUpdateRequest;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueStatusResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueTableResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

@Tag(name = "DB PII Issue", description = "DB 개인정보 이슈 관리 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbPiiIssueControllerDocs {

    @Operation(summary = "이슈 요약 통계 조회", description = "총 이슈 컬럼 수, 위험도별 이슈 수, 총 개인정보 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<DbPiiIssueSummaryResponseDTO> getSummary(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "테이블별 이슈 목록 조회", description = "테이블별로 그룹화된 이슈 목록을 무한스크롤로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<DbPiiIssueTableResponseDTO>> getIssueList(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size
    );

    @Operation(summary = "이슈 작업 상태 변경", description = "이슈의 작업 상태를 변경합니다. (ISSUE, RUNNING, DONE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태값"),
            @ApiResponse(responseCode = "404", description = "이슈를 찾을 수 없음")
    })
    CommonResponse<DbPiiIssueStatusResponseDTO> updateStatus(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId,
            DbPiiIssueStatusUpdateRequest request
    );

    @Operation(summary = "이슈 상세 조회", description = "이슈 상세 정보와 암호화되지 않은 데이터 샘플(최대 10개)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이슈를 찾을 수 없음")
    })
    CommonResponse<DbPiiIssueDetailResponseDTO> getIssueDetail(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId
    );
}
