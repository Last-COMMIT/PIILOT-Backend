package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiStatsDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.QDbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.QDbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.QDbTable;
import com.lastcommit.piilot.domain.dbscan.entity.QDbmsType;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.QPiiType;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbPiiColumnCustomRepositoryImpl implements DbPiiColumnCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QDbPiiColumn piiColumn = QDbPiiColumn.dbPiiColumn;
    private static final QDbTable dbTable = QDbTable.dbTable;
    private static final QDbServerConnection connection = QDbServerConnection.dbServerConnection;
    private static final QDbmsType dbmsType = QDbmsType.dbmsType;
    private static final QPiiType piiType = QPiiType.piiType;

    @Override
    public Slice<DbPiiColumn> findPiiColumnsWithFilters(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiCategory,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, tableId, piiCategory, encrypted, riskLevel, keyword);

        List<DbPiiColumn> content = queryFactory
                .selectFrom(piiColumn)
                .join(piiColumn.dbTable, dbTable).fetchJoin()
                .join(dbTable.dbServerConnection, connection).fetchJoin()
                .join(connection.dbmsType, dbmsType).fetchJoin()
                .join(piiColumn.piiType, piiType).fetchJoin()
                .where(whereClause)
                .orderBy(dbTable.lastScannedAt.desc(), piiColumn.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public DbPiiStatsDTO calculateStats(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiCategory,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, tableId, piiCategory, encrypted, riskLevel, keyword);

        Tuple result = queryFactory
                .select(
                        piiColumn.count(),
                        new CaseBuilder()
                                .when(piiColumn.riskLevel.eq(RiskLevel.HIGH))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        piiColumn.encRecordsCount.sum().coalesce(0L),
                        piiColumn.totalRecordsCount.sum().coalesce(0L)
                )
                .from(piiColumn)
                .join(piiColumn.dbTable, dbTable)
                .join(dbTable.dbServerConnection, connection)
                .join(piiColumn.piiType, piiType)
                .where(whereClause)
                .fetchOne();

        if (result == null) {
            return new DbPiiStatsDTO(0L, 0L, 0L, 0L);
        }

        return new DbPiiStatsDTO(
                result.get(0, Long.class),
                result.get(1, Long.class),
                result.get(2, Long.class),
                result.get(3, Long.class)
        );
    }

    private BooleanBuilder buildWhereClause(
            Long userId,
            Long connectionId,
            Long tableId,
            PiiCategory piiCategory,
            Boolean encrypted,
            RiskLevel riskLevel,
            String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(connection.user.id.eq(userId));

        if (connectionId != null) {
            builder.and(connection.id.eq(connectionId));
        }

        if (tableId != null) {
            builder.and(dbTable.id.eq(tableId));
        }

        if (piiCategory != null) {
            builder.and(piiType.type.eq(piiCategory));
        }

        if (encrypted != null) {
            if (encrypted) {
                builder.and(isEncrypted());
            } else {
                builder.and(isNotEncrypted());
            }
        }

        if (riskLevel != null) {
            builder.and(piiColumn.riskLevel.eq(riskLevel));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            builder.and(
                    piiColumn.name.containsIgnoreCase(trimmedKeyword)
                            .or(dbTable.name.containsIgnoreCase(trimmedKeyword))
            );
        }

        return builder;
    }

    private BooleanExpression isEncrypted() {
        return piiColumn.encRecordsCount.isNotNull()
                .and(piiColumn.totalRecordsCount.isNotNull())
                .and(piiColumn.encRecordsCount.eq(piiColumn.totalRecordsCount));
    }

    private BooleanExpression isNotEncrypted() {
        return piiColumn.encRecordsCount.isNull()
                .or(piiColumn.totalRecordsCount.isNull())
                .or(piiColumn.encRecordsCount.ne(piiColumn.totalRecordsCount));
    }
}
