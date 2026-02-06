package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.MaskingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaskingLogRepository extends JpaRepository<MaskingLog, Long> {

    // Cascade delete: 특정 connection의 모든 마스킹 로그 삭제
    void deleteByConnectionId(Long connectionId);
}
