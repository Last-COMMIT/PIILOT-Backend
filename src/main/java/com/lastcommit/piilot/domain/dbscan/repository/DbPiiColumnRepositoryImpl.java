package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiSearchCondition;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiFilterOptionDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lastcommit.piilot.domain.dbscan.entity.QDbPiiColumn.dbPiiColumn;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbServerConnection.dbServerConnection;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbTable.dbTable;
import static com.lastcommit.piilot.domain.dbscan.entity.QDbmsType.dbmsType;
import static com.lastcommit.piilot.domain.shared.QPiiType.piiType;

@Repository
@RequiredArgsConstructor
public class DbPiiColumnRepositoryImpl implements DbPiiColumnRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public DbPiiSummaryResponseDTO getSummary(Long userId) {
        // 총 개인정보 컬럼 수
        Long totalPiiColumns = queryFactory
                .select(dbPiiColumn.count())
                .from(dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(dbServerConnection.user.id.eq(userId))
                .fetchOne();

        // 고위험 컬럼 수
        Long highRiskColumns = queryFactory
                .select(dbPiiColumn.count())
                .from(dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(
                        dbServerConnection.user.id.eq(userId),
                        dbPiiColumn.riskLevel.eq(RiskLevel.HIGH)
                )
                .fetchOne();

        // 암호화율 및 총 레코드 수 계산
        Tuple recordCounts = queryFactory
                .select(
                        dbPiiColumn.totalRecordsCount.sum(),
                        dbPiiColumn.encRecordsCount.sum()
                )
                .from(dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(dbServerConnection.user.id.eq(userId))
                .fetchOne();

        long totalRecords = recordCounts.get(dbPiiColumn.totalRecordsCount.sum()) != null
                ? recordCounts.get(dbPiiColumn.totalRecordsCount.sum()) : 0L;
        long encRecords = recordCounts.get(dbPiiColumn.encRecordsCount.sum()) != null
                ? recordCounts.get(dbPiiColumn.encRecordsCount.sum()) : 0L;

        double encryptionRate = totalRecords > 0
                ? Math.round((encRecords * 100.0 / totalRecords) * 10) / 10.0
                : 0.0;

        return new DbPiiSummaryResponseDTO(
                totalPiiColumns != null ? totalPiiColumns : 0L,
                highRiskColumns != null ? highRiskColumns : 0L,
                encryptionRate,
                totalRecords
        );
    }

    @Override
    public Slice<DbPiiColumn> searchPiiColumns(Long userId, DbPiiSearchCondition condition, Pageable pageable) {
        List<DbPiiColumn> content = queryFactory
                .selectFrom(dbPiiColumn)
                .join(dbPiiColumn.dbTable, dbTable).fetchJoin()
                .join(dbTable.dbServerConnection, dbServerConnection).fetchJoin()
                .join(dbServerConnection.dbmsType, dbmsType).fetchJoin()
                .join(dbPiiColumn.piiType, piiType).fetchJoin()
                .where(
                        dbServerConnection.user.id.eq(userId),
                        connectionIdEq(condition.connectionId()),
                        tableIdEq(condition.tableId()),
                        piiTypeEq(condition.piiType()),
                        encryptedEq(condition.encrypted()),
                        riskLevelEq(condition.riskLevel()),
                        keywordContains(condition.keyword())
                )
                .orderBy(dbTable.lastScannedAt.desc().nullsLast(), dbPiiColumn.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<DbPiiFilterOptionDTO> getConnectionOptions(Long userId) {
        return queryFactory
                .select(Projections.constructor(DbPiiFilterOptionDTO.class,
                        dbServerConnection.id,
                        dbServerConnection.connectionName.concat(" (").concat(dbmsType.name).concat(")")
                ))
                .from(dbServerConnection)
                .join(dbServerConnection.dbmsType, dbmsType)
                .where(dbServerConnection.user.id.eq(userId))
                .orderBy(dbServerConnection.connectionName.asc())
                .fetch();
    }

    @Override
    public List<DbPiiFilterOptionDTO> getTableOptions(Long userId, Long connectionId) {
        return queryFactory
                .select(Projections.constructor(DbPiiFilterOptionDTO.class,
                        dbTable.id,
                        dbTable.name
                ))
                .from(dbTable)
                .join(dbTable.dbServerConnection, dbServerConnection)
                .where(
                        dbServerConnection.user.id.eq(userId),
                        connectionId != null ? dbServerConnection.id.eq(connectionId) : null
                )
                .orderBy(dbTable.name.asc())
                .fetch();
    }

    private BooleanExpression connectionIdEq(Long connectionId) {
        return connectionId != null ? dbServerConnection.id.eq(connectionId) : null;
    }

    private BooleanExpression tableIdEq(Long tableId) {
        return tableId != null ? dbTable.id.eq(tableId) : null;
    }

    private BooleanExpression piiTypeEq(String piiTypeStr) {
        if (piiTypeStr == null || piiTypeStr.isBlank()) {
            return null;
        }
        try {
            return piiType.type.eq(PiiCategory.valueOf(piiTypeStr));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BooleanExpression encryptedEq(Boolean encrypted) {
        if (encrypted == null) {
            return null;
        }
        if (encrypted) {
            // 완전 암호화: encRecordsCount == totalRecordsCount (둘 다 > 0)
            return dbPiiColumn.encRecordsCount.eq(dbPiiColumn.totalRecordsCount)
                    .and(dbPiiColumn.totalRecordsCount.gt(0L));
        } else {
            // 비암호화 또는 부분 암호화: 1개라도 암호화 안된 레코드가 있으면 해당
            return dbPiiColumn.encRecordsCount.isNull()
                    .or(dbPiiColumn.encRecordsCount.eq(0L))
                    .or(dbPiiColumn.encRecordsCount.lt(dbPiiColumn.totalRecordsCount));
        }
    }

    private BooleanExpression riskLevelEq(String riskLevelStr) {
        if (riskLevelStr == null || riskLevelStr.isBlank()) {
            return null;
        }
        try {
            return dbPiiColumn.riskLevel.eq(RiskLevel.valueOf(riskLevelStr));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return dbServerConnection.connectionName.containsIgnoreCase(keyword)
                .or(dbTable.name.containsIgnoreCase(keyword))
                .or(dbPiiColumn.name.containsIgnoreCase(keyword));
    }
}
