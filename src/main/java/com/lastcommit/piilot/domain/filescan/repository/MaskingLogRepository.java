package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.MaskingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaskingLogRepository extends JpaRepository<MaskingLog, Long> {
}
