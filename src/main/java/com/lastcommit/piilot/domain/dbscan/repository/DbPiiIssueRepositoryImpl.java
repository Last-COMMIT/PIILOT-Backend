package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueItemDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueTableResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lastcommit.piilot.domain.dbscan.entity.QDbPiiColumn.dbPiiColumn;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbPiiIssue.dbPiiIssue;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbServerConnection.dbServerConnection;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbTable.dbTable;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbmsType.dbmsType;
import static com.lastcommit.piilot.domain.shared.QPiiType.piiType;

@Repository
@RequiredArgsConstructor
public class DbPiiIssueRepositoryImpl implements DbPiiIssueRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public DbPiiIssueSummaryResponseDTO getSummary(Long userId) {
        // 총 이슈 컬럼 수
        Long totalIssueColumns = queryFactory
                .select(dbPiiIssue.count())
                .from(dbPiiIssue)
                .join(dbPiiIssue.dbPiiColumn, dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(
                        dbServerConnection.user.id.eq(userId),
                        dbPiiIssue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .fetchOne();

        // 위험도별 카운트
        List<Tuple> riskCounts = queryFactory
                .select(dbPiiColumn.riskLevel, dbPiiIssue.count())
                .from(dbPiiIssue)
                .join(dbPiiIssue.dbPiiColumn, dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(
                        dbServerConnection.user.id.eq(userId),
                        dbPiiIssue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .groupBy(dbPiiColumn.riskLevel)
                .fetch();

        long highRiskCount = 0L;
        long mediumRiskCount = 0L;
        long lowRiskCount = 0L;

        for (Tuple tuple : riskCounts) {
            RiskLevel level = tuple.get(dbPiiColumn.riskLevel);
            Long count = tuple.get(dbPiiIssue.count());
            if (level != null && count != null) {
                switch (level) {
                    case HIGH -> highRiskCount = count;
                    case MEDIUM -> mediumRiskCount = count;
                    case LOW -> lowRiskCount = count;
                }
            }
        }

        // 총 개인정보 수 (이슈가 있는 컬럼의 레코드 합계)
        Long totalRecords = queryFactory
                .select(dbPiiColumn.totalRecordsCount.sum())
                .from(dbPiiIssue)
                .join(dbPiiIssue.dbPiiColumn, dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(
                        dbServerConnection.user.id.eq(userId),
                        dbPiiIssue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .fetchOne();

        return new DbPiiIssueSummaryResponseDTO(
                totalIssueColumns != null ? totalIssueColumns : 0L,
                highRiskCount,
                mediumRiskCount,
                lowRiskCount,
                totalRecords != null ? totalRecords : 0L
        );
    }

    @Override
    public Slice<DbPiiIssueTableResponseDTO> getIssuesByTable(Long userId, Pageable pageable) {
        // 이슈 목록 조회 (테이블 정보 포함)
        List<DbPiiIssue> issues = queryFactory
                .selectFrom(dbPiiIssue)
                .join(dbPiiIssue.dbPiiColumn, dbPiiColumn).fetchJoin()
                .join(dbPiiColumn.dbTable, dbTable).fetchJoin()
                .join(dbTable.dbServerConnection, dbServerConnection).fetchJoin()
                .join(dbServerConnection.dbmsType, dbmsType).fetchJoin()
                .join(dbPiiColumn.piiType, piiType).fetchJoin()
                .where(
                        dbServerConnection.user.id.eq(userId),
                        dbPiiIssue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .orderBy(dbTable.id.asc(), dbPiiIssue.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = issues.size() > pageable.getPageSize();
        if (hasNext) {
            issues.remove(issues.size() - 1);
        }

        // 테이블별로 그룹화
        Map<Long, DbPiiIssueTableResponseDTO> tableMap = new LinkedHashMap<>();
        for (DbPiiIssue issue : issues) {
            Long tableId = issue.getDbPiiColumn().getDbTable().getId();

            if (!tableMap.containsKey(tableId)) {
                var table = issue.getDbPiiColumn().getDbTable();
                var connection = table.getDbServerConnection();

                String connectionInfo = connection.getConnectionName() +
                        " (" + connection.getDbmsType().getName() + ")";
                if (connection.getManagerName() != null && !connection.getManagerName().isBlank()) {
                    connectionInfo += " | 담당자: " + connection.getManagerName();
                }

                tableMap.put(tableId, new DbPiiIssueTableResponseDTO(
                        tableId,
                        table.getName(),
                        connectionInfo,
                        0,
                        new ArrayList<>()
                ));
            }

            DbPiiIssueTableResponseDTO tableDTO = tableMap.get(tableId);
            List<DbPiiIssueItemDTO> issueList = new ArrayList<>(tableDTO.issues());
            issueList.add(DbPiiIssueItemDTO.from(issue));

            tableMap.put(tableId, new DbPiiIssueTableResponseDTO(
                    tableDTO.tableId(),
                    tableDTO.tableName(),
                    tableDTO.connectionInfo(),
                    issueList.size(),
                    issueList
            ));
        }

        List<DbPiiIssueTableResponseDTO> content = new ArrayList<>(tableMap.values());
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Optional<DbPiiIssue> findByIdWithDetails(Long issueId, Long userId) {
        DbPiiIssue issue = queryFactory
                .selectFrom(dbPiiIssue)
                .join(dbPiiIssue.dbPiiColumn, dbPiiColumn).fetchJoin()
                .join(dbPiiColumn.dbTable, dbTable).fetchJoin()
                .join(dbTable.dbServerConnection, dbServerConnection).fetchJoin()
                .join(dbServerConnection.dbmsType, dbmsType).fetchJoin()
                .join(dbPiiColumn.piiType, piiType).fetchJoin()
                .where(
                        dbPiiIssue.id.eq(issueId),
                        dbServerConnection.user.id.eq(userId)
                )
                .fetchOne();

        return Optional.ofNullable(issue);
    }
}
