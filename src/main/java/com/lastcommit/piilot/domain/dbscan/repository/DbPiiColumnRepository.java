package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DbPiiColumnRepository extends JpaRepository<DbPiiColumn, Long>, DbPiiColumnCustomRepository {

    List<DbPiiColumn> findByDbTableId(Long tableId);

    List<DbPiiColumn> findByDbTableDbServerConnectionId(Long connectionId);
}
