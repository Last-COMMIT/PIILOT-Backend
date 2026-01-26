package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DbTableRepository extends JpaRepository<DbTable, Long> {

    long countByDbServerConnectionId(Long connectionId);

    @Query("SELECT COALESCE(SUM(t.totalColumns), 0) FROM DbTable t WHERE t.dbServerConnection.id = :connectionId")
    long sumTotalColumnsByConnectionId(@Param("connectionId") Long connectionId);
}
