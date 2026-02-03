package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiConnectionResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import com.lastcommit.piilot.global.validation.annotation.ValidPage;
import com.lastcommit.piilot.global.validation.annotation.ValidSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "File PII", description = "파일 개인정보 목록 API")
public interface FilePiiControllerDocs {

    @Operation(summary = "파일 서버 연결 목록 조회", description = "필터 드롭다운용 파일 서버 연결 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<FilePiiConnectionResponseDTO>> getConnections(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "파일 개인정보 목록 조회", description = "파일 개인정보 목록과 통계를 조회합니다. (필터/검색/페이징 지원)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 서버 연결 정보 없음")
    })
    CommonResponse<FilePiiListResponseDTO> getFiles(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "커넥션 ID") Long connectionId,
            @Parameter(description = "파일 카테고리 (DOCUMENT, PHOTO, VIDEO, AUDIO)") FileCategory category,
            @Parameter(description = "마스킹 여부 (true: 완료, false: 미완료)") Boolean masked,
            @Parameter(description = "위험도 (HIGH, MEDIUM, LOW)") RiskLevel riskLevel,
            @Parameter(description = "파일명 검색어") String keyword,
            @ValidPage @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @ValidSize @Parameter(description = "페이지 크기") int size
    );
}
