package com.lastcommit.piilot.domain.dbscan.repository;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiIssueStatsDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.QDbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.QDbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.QDbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.QDbTable;
import com.lastcommit.piilot.domain.dbscan.entity.QDbmsType;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.QPiiType;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbPiiIssueCustomRepositoryImpl implements DbPiiIssueCustomRepository {

    private final JPAQueryFactory queryFactory;

    private final QDbPiiIssue issue = QDbPiiIssue.dbPiiIssue;
    private final QDbPiiColumn column = QDbPiiColumn.dbPiiColumn;
    private final QDbTable table = QDbTable.dbTable;
    private final QDbServerConnection connection = QDbServerConnection.dbServerConnection;
    private final QDbmsType dbmsType = QDbmsType.dbmsType;
    private final QPiiType piiType = QPiiType.piiType;

    @Override
    public DbPiiIssueStatsDTO calculateStats(Long userId) {
        var result = queryFactory
                .select(
                        issue.count(),
                        new CaseBuilder()
                                .when(column.riskLevel.eq(RiskLevel.HIGH))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(column.riskLevel.eq(RiskLevel.MEDIUM))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(column.riskLevel.eq(RiskLevel.LOW))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        column.totalRecordsCount.sum().coalesce(0L)
                )
                .from(issue)
                .join(issue.dbPiiColumn, column)
                .join(column.dbTable, table)
                .join(table.dbServerConnection, connection)
                .where(
                        connection.user.id.eq(userId),
                        issue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .fetchOne();

        if (result == null) {
            return DbPiiIssueStatsDTO.empty();
        }

        return new DbPiiIssueStatsDTO(
                result.get(0, Long.class) != null ? result.get(0, Long.class) : 0L,
                result.get(1, Long.class) != null ? result.get(1, Long.class) : 0L,
                result.get(2, Long.class) != null ? result.get(2, Long.class) : 0L,
                result.get(3, Long.class) != null ? result.get(3, Long.class) : 0L,
                result.get(4, Long.class) != null ? result.get(4, Long.class) : 0L
        );
    }

    @Override
    public List<DbPiiIssue> findActiveIssuesWithDetails(Long userId) {
        return queryFactory
                .selectFrom(issue)
                .join(issue.dbPiiColumn, column).fetchJoin()
                .join(column.dbTable, table).fetchJoin()
                .join(table.dbServerConnection, connection).fetchJoin()
                .join(connection.dbmsType, dbmsType).fetchJoin()
                .join(column.piiType, piiType).fetchJoin()
                .where(
                        connection.user.id.eq(userId),
                        issue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .orderBy(issue.detectedAt.desc())
                .fetch();
    }
}
