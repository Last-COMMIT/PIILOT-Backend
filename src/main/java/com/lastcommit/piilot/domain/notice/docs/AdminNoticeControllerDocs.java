package com.lastcommit.piilot.domain.notice.docs;

import com.lastcommit.piilot.domain.notice.dto.request.NoticeCreateRequestDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Notice", description = "공지사항 관리 API (관리자 전용)")
@SecurityRequirement(name = "JWT TOKEN")
public interface AdminNoticeControllerDocs {

    @Operation(summary = "공지사항 작성", description = "새로운 공지사항을 작성합니다. (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    CommonResponse<NoticeResponseDTO> createNotice(
            @Parameter(hidden = true) Long userId,
            NoticeCreateRequestDTO request
    );

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    CommonResponse<Void> deleteNotice(
            @Parameter(description = "공지사항 ID") Long noticeId
    );
}
