package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DbServerConnectionRepository extends JpaRepository<DbServerConnection, Long> {

    boolean existsByConnectionNameAndUserId(String connectionName, Long userId);

    Slice<DbServerConnection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ConnectionStatus status);

    List<DbServerConnection> findByStatus(ConnectionStatus status);
}
