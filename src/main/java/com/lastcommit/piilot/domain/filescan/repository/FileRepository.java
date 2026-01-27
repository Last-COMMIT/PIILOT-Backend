package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, Long> {

    long countByConnectionId(Long connectionId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.connection.id = :connectionId")
    long sumFileSizeByConnectionId(@Param("connectionId") Long connectionId);

    long countByConnectionUserId(Long userId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.connection.user.id = :userId")
    long sumFileSizeByConnectionUserId(@Param("userId") Long userId);
}
