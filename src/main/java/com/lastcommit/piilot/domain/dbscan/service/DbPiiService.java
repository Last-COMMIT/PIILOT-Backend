package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiStatsDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.*;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import com.lastcommit.piilot.domain.dbscan.exception.DbPiiErrorStatus;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiColumnRepository;
import com.lastcommit.piilot.domain.dbscan.repository.DbServerConnectionRepository;
import com.lastcommit.piilot.domain.dbscan.repository.DbTableRepository;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.exception.GeneralException;
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

    private final DbServerConnectionRepository connectionRepository;
    private final DbTableRepository tableRepository;
    private final DbPiiColumnRepository piiColumnRepository;

    public List<DbPiiConnectionResponseDTO> getConnections(Long userId) {
        List<DbServerConnection> connections = connectionRepository.findByUserIdOrderByConnectionNameAsc(userId);
        return connections.stream()
                .map(DbPiiConnectionResponseDTO::from)
                .toList();
    }

    public List<DbPiiTableResponseDTO> getTables(Long userId, Long connectionId) {
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbPiiErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(DbPiiErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        List<DbTable> tables = tableRepository.findByDbServerConnectionIdOrderByNameAsc(connectionId);
        return tables.stream()
                .map(DbPiiTableResponseDTO::from)
                .toList();
    }

    public DbPiiColumnListResponseDTO getPiiColumns(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiType,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    ) {
        if (connectionId != null) {
            validateConnectionAccess(userId, connectionId);
        }

        if (tableId != null) {
            validateTableAccess(userId, tableId, connectionId);
        }

        DbPiiStatsDTO stats = piiColumnRepository.calculateStats(
                userId, connectionId, tableId, piiType, encrypted, riskLevel, keyword
        );

        Slice<DbPiiColumn> columns = piiColumnRepository.findPiiColumnsWithFilters(
                userId, connectionId, tableId, piiType, encrypted, riskLevel, keyword, pageable
        );

        Slice<DbPiiColumnResponseDTO> content = columns.map(DbPiiColumnResponseDTO::from);
        DbPiiStatsResponseDTO statsResponse = DbPiiStatsResponseDTO.from(stats);

        return DbPiiColumnListResponseDTO.of(statsResponse, content);
    }

    private void validateConnectionAccess(Long userId, Long connectionId) {
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbPiiErrorStatus.CONNECTION_NOT_FOUND));

        if (!connection.getUser().getId().equals(userId)) {
            throw new GeneralException(DbPiiErrorStatus.CONNECTION_ACCESS_DENIED);
        }
    }

    private void validateTableAccess(Long userId, Long tableId, Long connectionId) {
        DbTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new GeneralException(DbPiiErrorStatus.TABLE_NOT_FOUND));

        if (!table.getDbServerConnection().getUser().getId().equals(userId)) {
            throw new GeneralException(DbPiiErrorStatus.CONNECTION_ACCESS_DENIED);
        }

        if (connectionId != null && !table.getDbServerConnection().getId().equals(connectionId)) {
            throw new GeneralException(DbPiiErrorStatus.TABLE_CONNECTION_MISMATCH);
        }
    }
}
