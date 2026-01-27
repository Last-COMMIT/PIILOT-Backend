package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerConnectionRepository extends JpaRepository<FileServerConnection, Long> {

    boolean existsByConnectionNameAndUserId(String connectionName, Long userId);

    Slice<FileServerConnection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
