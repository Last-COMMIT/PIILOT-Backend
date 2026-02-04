package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DbPiiColumnRepository extends JpaRepository<DbPiiColumn, Long>, DbPiiColumnCustomRepository {

    // Cascade delete: 특정 테이블의 모든 PII 컬럼 삭제
    void deleteByDbTableId(Long tableId);

    // Cascade delete: 특정 connection의 모든 PII 컬럼 삭제
    void deleteByDbTableDbServerConnectionId(Long connectionId);

    List<DbPiiColumn> findByDbTableId(Long tableId);

    List<DbPiiColumn> findByDbTableDbServerConnectionId(Long connectionId);

    // Dashboard: 사용자의 전체 PII 컬럼 수
    @Query("SELECT COUNT(c) FROM DbPiiColumn c " +
            "WHERE c.dbTable.dbServerConnection.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Dashboard: 사용자의 전체 레코드 수 합계
    @Query("SELECT COALESCE(SUM(c.totalRecordsCount), 0) FROM DbPiiColumn c " +
            "WHERE c.dbTable.dbServerConnection.user.id = :userId")
    long sumTotalRecordsByUserId(@Param("userId") Long userId);

    // Dashboard: 사용자의 암호화된 레코드 수 합계
    @Query("SELECT COALESCE(SUM(c.encRecordsCount), 0) FROM DbPiiColumn c " +
            "WHERE c.dbTable.dbServerConnection.user.id = :userId")
    long sumEncRecordsByUserId(@Param("userId") Long userId);

    // Dashboard: PII 유형별 분포 (DB)
    @Query("SELECT c.piiType.type, COALESCE(SUM(c.totalRecordsCount), 0) FROM DbPiiColumn c " +
            "WHERE c.dbTable.dbServerConnection.user.id = :userId " +
            "GROUP BY c.piiType.type")
    List<Object[]> getPiiDistributionByUserId(@Param("userId") Long userId);
}
