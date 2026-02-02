package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiStatsDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface DbPiiColumnCustomRepository {

    Slice<DbPiiColumn> findPiiColumnsWithFilters(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiType,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    );

    DbPiiStatsDTO calculateStats(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiType,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword
    );
}
