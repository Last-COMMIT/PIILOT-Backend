package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbServerConnectionRepository extends JpaRepository<DbServerConnection, Long> {

    boolean existsByConnectionNameAndUserId(String connectionName, Long userId);

    Slice<DbServerConnection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
