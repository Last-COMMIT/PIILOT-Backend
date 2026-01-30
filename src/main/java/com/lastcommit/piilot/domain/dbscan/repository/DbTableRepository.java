package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DbTableRepository extends JpaRepository<DbTable, Long> {

    long countByDbServerConnectionId(Long connectionId);

    @Query("SELECT COALESCE(SUM(t.totalColumns), 0) FROM DbTable t WHERE t.dbServerConnection.id = :connectionId")
    long sumTotalColumnsByConnectionId(@Param("connectionId") Long connectionId);

    long countByDbServerConnectionUserId(Long userId);

    @Query("SELECT COALESCE(SUM(t.totalColumns), 0) FROM DbTable t WHERE t.dbServerConnection.user.id = :userId")
    long sumTotalColumnsByUserId(@Param("userId") Long userId);

    List<DbTable> findByDbServerConnectionId(Long connectionId);

    Optional<DbTable> findByDbServerConnectionIdAndName(Long connectionId, String name);

    List<DbTable> findByDbServerConnectionIdOrderByNameAsc(Long connectionId);
}
