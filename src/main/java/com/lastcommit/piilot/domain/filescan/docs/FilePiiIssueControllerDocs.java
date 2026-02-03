package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiIssueStatusUpdateRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiIssueStatusUpdateResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import com.lastcommit.piilot.global.validation.annotation.ValidPage;
import com.lastcommit.piilot.global.validation.annotation.ValidSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "File PII Issue", description = "파일 개인정보 이슈 관리 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface FilePiiIssueControllerDocs {

    @Operation(summary = "이슈 목록 조회", description = "파일 서버별로 그룹화된 파일 개인정보 이슈 목록과 통계를 조회합니다. 이슈가 많은 서버 순으로 정렬됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<FilePiiIssueListResponseDTO> getIssueList(
            @Parameter(hidden = true) Long userId,
            @ValidPage @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @ValidSize @Parameter(description = "페이지 크기 (서버 그룹 단위)") int size
    );

    @Operation(summary = "이슈 상세 조회", description = "파일 개인정보 이슈의 상세 정보와 개인정보 유형별 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    CommonResponse<FilePiiIssueDetailResponseDTO> getIssueDetail(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId
    );

    @Operation(summary = "작업 상태 변경", description = "파일 개인정보 이슈의 작업 상태를 변경합니다. (ISSUE → RUNNING → DONE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    CommonResponse<FilePiiIssueStatusUpdateResponseDTO> updateIssueStatus(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "이슈 ID") Long issueId,
            FilePiiIssueStatusUpdateRequestDTO request
    );
}
