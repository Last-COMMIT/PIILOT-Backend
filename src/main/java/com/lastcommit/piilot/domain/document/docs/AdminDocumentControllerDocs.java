package com.lastcommit.piilot.domain.document.docs;

import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Document", description = "문서 관리 API (관리자 전용)")
@SecurityRequirement(name = "JWT TOKEN")
public interface AdminDocumentControllerDocs {

    @Operation(summary = "파일 업로드", description = "법령/내규/DB사전 파일을 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 파일, 잘못된 유형, 크기 초과)"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "500", description = "파일 저장 실패")
    })
    CommonResponse<DocumentListResponseDTO> upload(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "업로드할 파일") MultipartFile file,
            @Parameter(description = "파일 유형 (LAWS, INTERNAL_REGULATIONS, DB_INFO)") String type
    );

    @Operation(summary = "파일 목록 조회", description = "업로드된 파일 목록을 조회합니다. (무한스크롤)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    CommonResponse<Slice<DocumentListResponseDTO>> getDocumentList(
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size
    );

    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "문서 없음")
    })
    CommonResponse<Void> delete(
            @Parameter(description = "문서 ID") Long documentId
    );
}
