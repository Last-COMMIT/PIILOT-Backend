package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileServerConnectionRepository extends JpaRepository<FileServerConnection, Long> {

    boolean existsByConnectionNameAndUserId(String connectionName, Long userId);

    Slice<FileServerConnection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ConnectionStatus status);

    List<FileServerConnection> findByStatus(ConnectionStatus status);

    @Query("SELECT fc FROM FileServerConnection fc " +
           "JOIN FETCH fc.serverType " +
           "WHERE fc.user.id = :userId " +
           "ORDER BY fc.connectionName ASC")
    List<FileServerConnection> findByUserIdOrderByConnectionNameAsc(@Param("userId") Long userId);
}
