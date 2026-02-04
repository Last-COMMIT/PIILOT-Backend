package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbScanHistoryRepository extends JpaRepository<DbScanHistory, Long> {

    // Cascade delete: 특정 connection의 모든 스캔 이력 삭제
    void deleteByDbServerConnectionId(Long connectionId);
}
