package com.lastcommit.piilot.domain.notification.docs;

import com.lastcommit.piilot.domain.notification.dto.response.NotificationResponseDTO;
import com.lastcommit.piilot.domain.notification.dto.response.NotificationStatsResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;

import java.util.List;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationControllerDocs {

    @Operation(
        summary = "알림 목록 조회",
        description = "사용자의 알림 목록을 조회합니다 (무한스크롤)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<Slice<NotificationResponseDTO>> getNotifications(
        @Parameter(hidden = true) Long userId,
        @Parameter(description = "페이지 번호 (0부터 시작)") int page,
        @Parameter(description = "페이지 크기") int size
    );

    @Operation(
        summary = "최근 읽지 않은 알림 조회",
        description = "헤더 드롭다운용 - 최근 읽지 않은 알림 3개 조회"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<List<NotificationResponseDTO>> getRecentUnread(
        @Parameter(hidden = true) Long userId
    );

    @Operation(
        summary = "읽지 않은 알림 수 조회",
        description = "뱃지 표시용 - 읽지 않은 알림 개수 조회"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CommonResponse<NotificationStatsResponseDTO> getUnreadCount(
        @Parameter(hidden = true) Long userId
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 상태로 변경합니다"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "404", description = "알림 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    CommonResponse<NotificationResponseDTO> markAsRead(
        @Parameter(hidden = true) Long userId,
        @Parameter(description = "알림 ID") Long notificationId
    );
}
