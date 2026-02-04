package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long>, FileRepositoryCustom {

    long countByConnectionId(Long connectionId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.connection.id = :connectionId")
    long sumFileSizeByConnectionId(@Param("connectionId") Long connectionId);

    long countByConnectionUserId(Long userId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.connection.user.id = :userId")
    long sumFileSizeByConnectionUserId(@Param("userId") Long userId);

    Optional<File> findByConnectionIdAndFilePath(Long connectionId, String filePath);

    @Query("SELECT f.filePath FROM File f WHERE f.connection.id = :connectionId")
    List<String> findFilePathsByConnectionId(@Param("connectionId") Long connectionId);

    List<File> findByConnectionId(Long connectionId);

    // Cascade delete: 특정 connection의 모든 파일 삭제
    void deleteByConnectionId(Long connectionId);

    // Dashboard: 개인정보 포함 파일 수 (hasPersonalInfo = true)
    @Query("SELECT COUNT(f) FROM File f " +
            "WHERE f.connection.user.id = :userId " +
            "AND f.hasPersonalInfo = true")
    long countPiiFilesByUserId(@Param("userId") Long userId);

    // Dashboard: 암호화된 개인정보 포함 파일 수
    @Query("SELECT COUNT(f) FROM File f " +
            "WHERE f.connection.user.id = :userId " +
            "AND f.hasPersonalInfo = true " +
            "AND f.isEncrypted = true")
    long countEncryptedPiiFilesByUserId(@Param("userId") Long userId);
}
