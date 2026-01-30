package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbScanHistoryRepository extends JpaRepository<DbScanHistory, Long> {
}
