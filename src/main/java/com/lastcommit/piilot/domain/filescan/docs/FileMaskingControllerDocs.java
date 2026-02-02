package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.request.FileMaskingSaveRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "File Masking", description = "파일 마스킹 API")
public interface FileMaskingControllerDocs {

    @Operation(summary = "커넥션 목록 조회", description = "마스킹 대상 파일이 있는 커넥션 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<FileMaskingConnectionResponseDTO>> getConnections(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "이슈 파일 목록 조회", description = "마스킹 대상 파일 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<FileMaskingFileResponseDTO>> getFiles(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "커넥션 ID") Long connectionId,
            @Parameter(description = "파일 유형 (DOCUMENT, PHOTO, AUDIO, VIDEO)") FileCategory fileCategory,
            @Parameter(description = "위험도 수준 (HIGH, MEDIUM, LOW)") RiskLevel riskLevel,
            @Parameter(description = "파일명 검색어") String fileName
    );

    @Operation(summary = "파일 미리보기", description = "원본 파일의 미리보기를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "파일 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 없음")
    })
    CommonResponse<FileMaskingPreviewResponseDTO> getFilePreview(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "파일 ID", required = true) Long fileId
    );

    @Operation(summary = "파일 마스킹", description = "AI를 사용하여 파일을 마스킹 처리합니다. 결과는 Redis에 30분간 캐싱됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마스킹 성공"),
            @ApiResponse(responseCode = "400", description = "마스킹 대상 파일이 아님"),
            @ApiResponse(responseCode = "403", description = "파일 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 없음"),
            @ApiResponse(responseCode = "503", description = "AI 서버 연결 실패")
    })
    CommonResponse<FileMaskingMaskResponseDTO> maskFile(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "파일 ID", required = true) Long fileId
    );

    @Operation(summary = "마스킹 결과 저장", description = "마스킹된 파일을 저장하고 원본 파일을 암호화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "마스킹 결과가 만료됨"),
            @ApiResponse(responseCode = "403", description = "파일 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 없음")
    })
    CommonResponse<FileMaskingSaveResponseDTO> saveMaskedFile(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "파일 ID", required = true) Long fileId,
            FileMaskingSaveRequestDTO request
    );
}
