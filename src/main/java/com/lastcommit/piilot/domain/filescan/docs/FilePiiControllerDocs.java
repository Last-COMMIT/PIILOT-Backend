package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiFilterOptionDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiSummaryResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

import java.util.List;

@Tag(name = "File PII", description = "파일 개인정보 목록 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface FilePiiControllerDocs {

    @Operation(summary = "파일 개인정보 요약 조회", description = "사용자의 파일 개인정보 요약 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<FilePiiSummaryResponseDTO> getSummary(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "파일 개인정보 목록 조회", description = "파일 개인정보 목록을 필터링하여 무한스크롤로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<FilePiiListResponseDTO>> getFilePiiList(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size,
            @Parameter(description = "파일 서버 연결 ID") Long connectionId,
            @Parameter(description = "파일 형식 (DOCUMENT, PHOTO, AUDIO, VIDEO)") String fileCategory,
            @Parameter(description = "마스킹 여부 (true: 마스킹됨, false: 미마스킹/부분)") Boolean masked,
            @Parameter(description = "위험도 수준 (HIGH, MEDIUM, LOW)") String riskLevel,
            @Parameter(description = "검색어 (연결명, 파일명, 경로)") String keyword
    );

    @Operation(summary = "파일 서버 연결 드롭다운 옵션 조회", description = "필터용 파일 서버 연결 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<FilePiiFilterOptionDTO>> getConnectionOptions(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "파일 형식 드롭다운 옵션 조회", description = "필터용 파일 형식 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<FilePiiFilterOptionDTO>> getFileCategoryOptions();
}
