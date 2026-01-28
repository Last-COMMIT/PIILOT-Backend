package com.lastcommit.piilot.domain.notice.service;

import com.lastcommit.piilot.domain.notice.dto.request.NoticeCreateRequestDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeDetailResponseDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeListResponseDTO;
import com.lastcommit.piilot.domain.notice.dto.response.NoticeResponseDTO;
import com.lastcommit.piilot.domain.notice.entity.Notice;
import com.lastcommit.piilot.domain.notice.exception.NoticeErrorStatus;
import com.lastcommit.piilot.domain.notice.repository.NoticeRepository;
import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.error.status.CommonErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoticeResponseDTO createNotice(Long userId, NoticeCreateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        Notice notice = Notice.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .build();

        Notice saved = noticeRepository.save(notice);
        return NoticeResponseDTO.from(saved);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new GeneralException(NoticeErrorStatus.NOTICE_NOT_FOUND));

        noticeRepository.delete(notice);
    }

    public Slice<NoticeListResponseDTO> getNoticeList(Pageable pageable) {
        Slice<Notice> notices = noticeRepository.findAllWithUserOrderByCreatedAtDesc(pageable);
        return notices.map(NoticeListResponseDTO::from);
    }

    public NoticeDetailResponseDTO getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findByIdWithUser(noticeId)
                .orElseThrow(() -> new GeneralException(NoticeErrorStatus.NOTICE_NOT_FOUND));

        return NoticeDetailResponseDTO.from(notice);
    }
}
