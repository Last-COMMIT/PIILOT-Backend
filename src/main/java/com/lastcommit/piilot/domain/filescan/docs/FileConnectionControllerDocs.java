package com.lastcommit.piilot.domain.filescan.docs;

import com.lastcommit.piilot.domain.filescan.dto.request.FileConnectionRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionDetailResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileConnectionStatsResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

@Tag(name = "File Connection", description = "파일 서버 연결 관리 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface FileConnectionControllerDocs {

    @Operation(summary = "파일 서버 연결 생성", description = "새로운 파일 서버 연결을 생성합니다. FTP, SFTP만 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (비밀번호 누락, 지원하지 않는 서버 유형)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 서버 유형을 찾을 수 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "연결 이름 중복",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    CommonResponse<FileConnectionResponseDTO> createConnection(
            @Parameter(hidden = true) Long userId,
            FileConnectionRequestDTO request
    );

    @Operation(summary = "파일 서버 연결 수정", description = "기존 파일 서버 연결 정보를 수정합니다. 비밀번호를 입력하지 않으면 기존 비밀번호가 유지됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "연결 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "연결 이름 중복",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    CommonResponse<FileConnectionResponseDTO> updateConnection(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID", required = true) Long connectionId,
            FileConnectionRequestDTO request
    );

    @Operation(summary = "파일 서버 연결 삭제", description = "파일 서버 연결 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "연결 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    CommonResponse<Void> deleteConnection(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID", required = true) Long connectionId
    );

    @Operation(summary = "파일 서버 연결 상세 조회", description = "파일 서버 연결의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "연결 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    CommonResponse<FileConnectionDetailResponseDTO> getConnectionDetail(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "연결 ID", required = true) Long connectionId
    );

    @Operation(summary = "파일 서버 연결 목록 조회", description = "사용자의 파일 서버 연결 목록을 무한 스크롤 방식으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<FileConnectionListResponseDTO>> getConnectionList(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size
    );

    @Operation(summary = "파일 서버 연결 통계 조회", description = "사용자의 파일 서버 연결 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<FileConnectionStatsResponseDTO> getConnectionStats(
            @Parameter(hidden = true) Long userId
    );
}
