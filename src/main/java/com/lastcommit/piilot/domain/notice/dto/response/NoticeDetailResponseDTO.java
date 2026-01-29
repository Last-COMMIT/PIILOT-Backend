package com.lastcommit.piilot.domain.notice.dto.response;

import com.lastcommit.piilot.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeDetailResponseDTO(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createdAt
) {
    public static NoticeDetailResponseDTO from(Notice notice) {
        return new NoticeDetailResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getUser().getName(),
                notice.getCreatedAt()
        );
    }
}
