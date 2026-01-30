package com.lastcommit.piilot.domain.notice.dto.response;

import com.lastcommit.piilot.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeListResponseDTO(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt
) {
    public static NoticeListResponseDTO from(Notice notice) {
        return new NoticeListResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getUser().getName(),
                notice.getCreatedAt()
        );
    }
}
