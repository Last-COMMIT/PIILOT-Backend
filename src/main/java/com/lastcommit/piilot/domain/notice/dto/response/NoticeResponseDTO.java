package com.lastcommit.piilot.domain.notice.dto.response;

import com.lastcommit.piilot.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeResponseDTO(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt
) {
    public static NoticeResponseDTO from(Notice notice) {
        return new NoticeResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getUser().getName(),
                notice.getCreatedAt()
        );
    }
}
