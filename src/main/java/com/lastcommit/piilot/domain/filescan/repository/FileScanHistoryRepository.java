package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FileScanHistory;
import com.lastcommit.piilot.domain.shared.ScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileScanHistoryRepository extends JpaRepository<FileScanHistory, Long> {

    boolean existsByFileServerConnectionIdAndStatus(Long connectionId, ScanStatus status);

    Optional<FileScanHistory> findByIdAndFileServerConnectionId(Long id, Long connectionId);
}
