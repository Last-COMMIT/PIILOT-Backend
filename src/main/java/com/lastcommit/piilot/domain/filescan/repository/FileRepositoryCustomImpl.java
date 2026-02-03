package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiStatsDTO;
import com.lastcommit.piilot.domain.filescan.entity.*;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FileRepositoryCustomImpl implements FileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QFile file = QFile.file;
    private static final QFileServerConnection connection = QFileServerConnection.fileServerConnection;
    private static final QFileType fileType = QFileType.fileType;
    private static final QFileServerType serverType = QFileServerType.fileServerType;
    private static final QFilePii filePii = QFilePii.filePii;

    @Override
    public Slice<File> findFilesWithFilters(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, category, masked, riskLevel, keyword);

        List<File> content = queryFactory
                .selectFrom(file)
                .join(file.connection, connection).fetchJoin()
                .join(connection.serverType, serverType).fetchJoin()
                .join(file.fileType, fileType).fetchJoin()
                .where(whereClause)
                .orderBy(file.lastScannedAt.desc(), file.id.desc())
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
    public FilePiiStatsDTO calculateStats(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, category, masked, riskLevel, keyword);

        Tuple result = queryFactory
                .select(
                        file.count(),
                        new CaseBuilder()
                                .when(file.riskLevel.eq(RiskLevel.HIGH))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        file.fileSize.sum().coalesce(0L)
                )
                .from(file)
                .join(file.connection, connection)
                .join(file.fileType, fileType)
                .where(whereClause)
                .fetchOne();

        if (result == null) {
            return new FilePiiStatsDTO(0L, 0L, 0L);
        }

        return new FilePiiStatsDTO(
                result.get(0, Long.class),
                result.get(1, Long.class),
                result.get(2, Long.class)
        );
    }

    @Override
    public Long countMaskedFiles(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword
    ) {
        // 마스킹 완료된 파일만 카운트 (모든 FilePii가 완전히 마스킹됨)
        BooleanBuilder whereClause = buildWhereClause(userId, connectionId, category, null, riskLevel, keyword);

        return queryFactory
                .select(file.count())
                .from(file)
                .join(file.connection, connection)
                .join(file.fileType, fileType)
                .where(
                        whereClause,
                        JPAExpressions
                                .selectFrom(filePii)
                                .where(
                                        filePii.file.eq(file),
                                        filePii.totalPiisCount.ne(filePii.maskedPiisCount)
                                )
                                .notExists()
                )
                .fetchOne();
    }

    private BooleanBuilder buildWhereClause(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(connection.user.id.eq(userId));
        builder.and(file.hasPersonalInfo.isTrue());

        if (connectionId != null) {
            builder.and(connection.id.eq(connectionId));
        }

        if (category != null) {
            builder.and(fileType.type.eq(category));
        }

        if (masked != null) {
            if (masked) {
                // 모든 FilePii가 완전히 마스킹된 파일만
                builder.and(
                        JPAExpressions
                                .selectFrom(filePii)
                                .where(
                                        filePii.file.eq(file),
                                        filePii.totalPiisCount.ne(filePii.maskedPiisCount)
                                )
                                .notExists()
                );
            } else {
                // 하나라도 미마스킹된 FilePii가 있는 파일
                builder.and(
                        JPAExpressions
                                .selectFrom(filePii)
                                .where(
                                        filePii.file.eq(file),
                                        filePii.totalPiisCount.ne(filePii.maskedPiisCount)
                                )
                                .exists()
                );
            }
        }

        if (riskLevel != null) {
            builder.and(file.riskLevel.eq(riskLevel));
        }

        if (keyword != null && !keyword.isBlank()) {
            builder.and(file.name.containsIgnoreCase(keyword.trim()));
        }

        return builder;
    }
}
