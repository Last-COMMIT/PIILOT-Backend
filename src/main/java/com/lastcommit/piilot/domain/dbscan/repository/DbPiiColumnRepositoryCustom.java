package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiSearchCondition;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiFilterOptionDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DbPiiColumnRepositoryCustom {

    DbPiiSummaryResponseDTO getSummary(Long userId);

    Slice<DbPiiColumn> searchPiiColumns(Long userId, DbPiiSearchCondition condition, Pageable pageable);

    List<DbPiiFilterOptionDTO> getConnectionOptions(Long userId);

    List<DbPiiFilterOptionDTO> getTableOptions(Long userId, Long connectionId);
}
