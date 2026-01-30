package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiSearchCondition;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiFilterOptionDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbPiiService {

    private final DbPiiColumnRepository dbPiiColumnRepository;

    public DbPiiSummaryResponseDTO getSummary(Long userId) {
        return dbPiiColumnRepository.getSummary(userId);
    }

    public Slice<DbPiiListResponseDTO> getPiiList(Long userId, DbPiiSearchCondition condition, Pageable pageable) {
        Slice<DbPiiColumn> piiColumns = dbPiiColumnRepository.searchPiiColumns(userId, condition, pageable);
        return piiColumns.map(DbPiiListResponseDTO::from);
    }

    public List<DbPiiFilterOptionDTO> getConnectionOptions(Long userId) {
        return dbPiiColumnRepository.getConnectionOptions(userId);
    }

    public List<DbPiiFilterOptionDTO> getTableOptions(Long userId, Long connectionId) {
        return dbPiiColumnRepository.getTableOptions(userId, connectionId);
    }
}
