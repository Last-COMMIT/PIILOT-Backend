package com.lastcommit.piilot.domain.document.docs;

import com.lastcommit.piilot.domain.document.dto.request.DocumentSaveRequestDTO;
import com.lastcommit.piilot.domain.document.dto.request.PresignedUrlRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentListResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.DocumentSaveResponseDTO;
import com.lastcommit.piilot.domain.document.dto.response.PresignedUrlResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Document", description = "문서 업로드 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface DocumentControllerDocs {

    @Operation(summary = "Presigned URL 생성", description = "S3 업로드를 위한 Presigned URL을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "Presigned URL 생성 실패")
    })
    CommonResponse<PresignedUrlResponseDTO> generatePresignedUrl(
            @Parameter(hidden = true) Long userId,
            PresignedUrlRequestDTO request
    );

    @Operation(summary = "문서 저장", description = "S3에 업로드된 문서 정보를 저장하고 AI 임베딩을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문서 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    CommonResponse<DocumentSaveResponseDTO> saveDocument(
            @Parameter(hidden = true) Long userId,
            DocumentSaveRequestDTO request
    );

    @Operation(summary = "문서 목록 조회", description = "사용자가 업로드한 문서 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<DocumentListResponseDTO>> getDocuments(
            @Parameter(hidden = true) Long userId
    );
}
