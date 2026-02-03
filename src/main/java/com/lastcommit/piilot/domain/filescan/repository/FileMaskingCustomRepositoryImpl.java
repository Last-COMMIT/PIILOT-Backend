package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.entity.QFile;
import com.lastcommit.piilot.domain.filescan.entity.QFilePii;
import com.lastcommit.piilot.domain.filescan.entity.QFileServerConnection;
import com.lastcommit.piilot.domain.filescan.entity.QFileType;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FileMaskingCustomRepositoryImpl implements FileMaskingCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QFile file = QFile.file;
    private static final QFileServerConnection connection = QFileServerConnection.fileServerConnection;
    private static final QFileType fileType = QFileType.fileType;
    private static final QFilePii filePii = QFilePii.filePii;

    @Override
    public List<File> findIssueFilesWithFilters(
            Long userId,
            Long connectionId,
            FileCategory fileCategory,
            RiskLevel riskLevel,
            String fileName
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, fileCategory, riskLevel, fileName);

        return queryFactory
                .selectFrom(file)
                .join(file.connection, connection).fetchJoin()
                .join(file.fileType, fileType).fetchJoin()
                .where(whereClause)
                .orderBy(file.riskLevel.desc(), file.lastModifiedTime.desc())
                .fetch();
    }

    private BooleanBuilder buildWhereClause(
            Long userId,
            Long connectionId,
            FileCategory fileCategory,
            RiskLevel riskLevel,
            String fileName
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: 사용자 소유 파일
        builder.and(connection.user.id.eq(userId));

        // 이슈 파일 조건
        builder.and(file.hasPersonalInfo.eq(true));
        builder.and(file.isEncrypted.eq(false));
        builder.and(file.isIssueOpen.eq(true));

        // 미마스킹 PII 존재 조건: maskedPiisCount < totalPiisCount
        builder.and(
                JPAExpressions
                        .selectOne()
                        .from(filePii)
                        .where(
                                filePii.file.id.eq(file.id),
                                filePii.maskedPiisCount.lt(filePii.totalPiisCount)
                        )
                        .exists()
        );

        // 필터 조건
        if (connectionId != null) {
            builder.and(connection.id.eq(connectionId));
        }

        if (fileCategory != null) {
            builder.and(fileType.type.eq(fileCategory));
        }

        if (riskLevel != null) {
            builder.and(file.riskLevel.eq(riskLevel));
        }

        if (fileName != null && !fileName.isBlank()) {
            builder.and(file.name.containsIgnoreCase(fileName.trim()));
        }

        return builder;
    }
}
