package com.lastcommit.piilot.domain.notice.repository;

import com.lastcommit.piilot.domain.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n JOIN FETCH n.user ORDER BY n.createdAt DESC")
    Slice<Notice> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT n FROM Notice n JOIN FETCH n.user WHERE n.id = :noticeId")
    Optional<Notice> findByIdWithUser(@Param("noticeId") Long noticeId);
}
