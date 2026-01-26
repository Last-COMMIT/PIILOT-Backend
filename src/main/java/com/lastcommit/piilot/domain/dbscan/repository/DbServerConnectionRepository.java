package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbServerConnectionRepository extends JpaRepository<DbServerConnection, Long> {

    boolean existsByConnectionNameAndUserId(String connectionName, Long userId);
}
