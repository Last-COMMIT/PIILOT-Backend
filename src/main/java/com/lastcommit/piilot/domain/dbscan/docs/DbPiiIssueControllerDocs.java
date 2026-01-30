package com.lastcommit.piilot.domain.dbscan.docs;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiIssueStatusUpdateRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueStatusUpdateResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "DB PII Issue", description = "DB 개인정보 이슈 관리 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DbPiiIssueControllerDocs {

    @Operation(summary = "이슈 목록 조회", description = "테이블별로 그룹화된 DB 개인정보 이슈 목록과 통계를 조회합니다. 이슈가 많은 테이블 순으로 정렬됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<DbPiiIssueListResponseDTO> getIssueList(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기 (테이블 그룹 단위)") int size
    );

    @Operation(summary = "이슈 상세 조회", description = "DB 개인정보 이슈의 상세 정보와 비암호화 데이터 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    CommonResponse<DbPiiIssueDetailResponseDTO> getIssueDetail(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId
    );

    @Operation(summary = "작업 상태 변경", description = "DB 개인정보 이슈의 작업 상태를 변경합니다. (ISSUE → RUNNING → DONE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    CommonResponse<DbPiiIssueStatusUpdateResponseDTO> updateIssueStatus(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId,
            DbPiiIssueStatusUpdateRequestDTO request
    );
}
