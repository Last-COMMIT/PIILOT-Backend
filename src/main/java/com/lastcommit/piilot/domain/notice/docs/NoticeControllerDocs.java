package com.lastcommit.piilot.domain.notice.docs;

import com.lastcommit.piilot.domain.notice.dto.response.NoticeDetailResponseDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeListResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

@Tag(name = "Notice", description = "공지사항 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface NoticeControllerDocs {

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<NoticeListResponseDTO>> getNoticeList(
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기") int size
    );

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    CommonResponse<NoticeDetailResponseDTO> getNoticeDetail(
            @Parameter(description = "공지사항 ID") Long noticeId
    );
}
