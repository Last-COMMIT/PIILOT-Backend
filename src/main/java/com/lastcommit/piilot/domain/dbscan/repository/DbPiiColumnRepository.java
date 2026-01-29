package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbPiiColumnRepository extends JpaRepository<DbPiiColumn, Long>, DbPiiColumnRepositoryCustom {
}
