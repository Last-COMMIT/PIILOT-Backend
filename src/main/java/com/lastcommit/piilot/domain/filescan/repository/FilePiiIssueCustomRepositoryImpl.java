package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiIssueStatsDTO;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.filescan.entity.QFile;
import com.lastcommit.piilot.domain.filescan.entity.QFilePii;
import com.lastcommit.piilot.domain.filescan.entity.QFilePiiIssue;
import com.lastcommit.piilot.domain.filescan.entity.QFileServerConnection;
import com.lastcommit.piilot.domain.filescan.entity.QFileServerType;
import com.lastcommit.piilot.domain.filescan.entity.QFileType;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FilePiiIssueCustomRepositoryImpl implements FilePiiIssueCustomRepository {

    private final JPAQueryFactory queryFactory;

    private final QFilePiiIssue issue = QFilePiiIssue.filePiiIssue;
    private final QFile file = QFile.file;
    private final QFilePii filePii = QFilePii.filePii;
    private final QFileServerConnection connection = QFileServerConnection.fileServerConnection;
    private final QFileServerType serverType = QFileServerType.fileServerType;
    private final QFileType fileType = QFileType.fileType;

    @Override
    public FilePiiIssueStatsDTO calculateStats(Long userId) {
        // 서브쿼리를 위한 별도 alias
        QFilePii subFilePii = new QFilePii("subFilePii");

        var result = queryFactory
                .select(
                        issue.count(),
                        new CaseBuilder()
                                .when(file.riskLevel.eq(RiskLevel.HIGH))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(file.riskLevel.eq(RiskLevel.MEDIUM))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(file.riskLevel.eq(RiskLevel.LOW))
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                )
                .from(issue)
                .join(issue.file, file)
                .join(issue.connection, connection)
                .where(
                        connection.user.id.eq(userId),
                        issue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .fetchOne();

        if (result == null) {
            return FilePiiIssueStatsDTO.empty();
        }

        // 총 PII 개수를 별도 쿼리로 조회
        Long totalPiiCount = queryFactory
                .select(filePii.totalPiisCount.sum().coalesce(0).longValue())
                .from(filePii)
                .join(filePii.file, file)
                .join(file.connection, connection)
                .join(issue).on(issue.file.eq(file).and(issue.issueStatus.eq(IssueStatus.ACTIVE)))
                .where(connection.user.id.eq(userId))
                .fetchOne();

        return new FilePiiIssueStatsDTO(
                result.get(0, Long.class) != null ? result.get(0, Long.class) : 0L,
                result.get(1, Long.class) != null ? result.get(1, Long.class) : 0L,
                result.get(2, Long.class) != null ? result.get(2, Long.class) : 0L,
                result.get(3, Long.class) != null ? result.get(3, Long.class) : 0L,
                totalPiiCount != null ? totalPiiCount : 0L
        );
    }

    @Override
    public List<FilePiiIssue> findActiveIssuesWithDetails(Long userId) {
        return queryFactory
                .selectFrom(issue)
                .join(issue.file, file).fetchJoin()
                .join(issue.connection, connection).fetchJoin()
                .join(connection.serverType, serverType).fetchJoin()
                .join(file.fileType, fileType).fetchJoin()
                .where(
                        connection.user.id.eq(userId),
                        issue.issueStatus.eq(IssueStatus.ACTIVE)
                )
                .orderBy(issue.detectedAt.desc())
                .fetch();
    }
}
