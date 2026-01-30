package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiSearchCondition;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lastcommit.piilot.domain.filescan.entity.QFile.file;
import static com.lastcommit.piilot.domain.filescan.entity.QFilePii.filePii;
import static com.lastcommit.piilot.domain.filescan.entity.QFileServerConnection.fileServerConnection;
import static com.lastcommit.piilot.domain.filescan.entity.QFileServerType.fileServerType;
import static com.lastcommit.piilot.domain.filescan.entity.QFileType.fileType;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<FilePiiListResponseDTO> searchFilePiiList(Long userId, FilePiiSearchCondition condition, Pageable pageable) {
        // 파일 엔티티 조회
        List<File> files = queryFactory
                .selectFrom(file)
                .join(file.connection, fileServerConnection).fetchJoin()
                .join(fileServerConnection.serverType, fileServerType).fetchJoin()
                .join(file.fileType, fileType).fetchJoin()
                .where(
                        fileServerConnection.user.id.eq(userId),
                        file.hasPersonalInfo.isTrue(),
                        connectionIdEq(condition.connectionId()),
                        fileCategoryEq(condition.fileCategory()),
                        maskedEq(condition.masked()),
                        riskLevelEq(condition.riskLevel()),
                        keywordContains(condition.keyword())
                )
                .orderBy(file.lastScannedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = files.size() > pageable.getPageSize();
        if (hasNext) {
            files.remove(files.size() - 1);
        }

        // 엔티티를 DTO로 변환하며 마스킹 정보 계산
        List<FilePiiListResponseDTO> content = files.stream()
                .map(f -> {
                    // 마스킹 정보 조회
                    Integer totalPiis = queryFactory
                            .select(filePii.totalPiisCount.sum().coalesce(0))
                            .from(filePii)
                            .where(filePii.file.id.eq(f.getId()))
                            .fetchOne();

                    Integer maskedPiis = queryFactory
                            .select(filePii.maskedPiisCount.sum().coalesce(0))
                            .from(filePii)
                            .where(filePii.file.id.eq(f.getId()))
                            .fetchOne();

                    int total = totalPiis != null ? totalPiis : 0;
                    int masked = maskedPiis != null ? maskedPiis : 0;

                    String maskingStatus;
                    double maskingRate;

                    if (total == 0 || masked == 0) {
                        maskingStatus = "NOT_MASKED";
                        maskingRate = 0.0;
                    } else if (masked >= total) {
                        maskingStatus = "MASKED";
                        maskingRate = 100.0;
                    } else {
                        maskingStatus = "PARTIAL";
                        maskingRate = (double) masked / total * 100;
                    }

                    String connectionName = f.getConnection().getConnectionName() +
                            " (" + f.getConnection().getServerType().getName() + ")";

                    FileCategory category = f.getFileType().getType();

                    return new FilePiiListResponseDTO(
                            f.getId(),
                            connectionName,
                            f.getName(),
                            f.getFilePath(),
                            category.name(),
                            category.getDisplayName(),
                            maskingStatus,
                            maskingRate,
                            f.getRiskLevel() != null ? f.getRiskLevel().name() : null,
                            f.getLastScannedAt()
                    );
                })
                .toList();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public long countFilesWithPersonalInfo(Long userId) {
        Long count = queryFactory
                .select(file.count())
                .from(file)
                .join(file.connection, fileServerConnection)
                .where(
                        fileServerConnection.user.id.eq(userId),
                        file.hasPersonalInfo.isTrue()
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public long countHighRiskFiles(Long userId) {
        Long count = queryFactory
                .select(file.count())
                .from(file)
                .join(file.connection, fileServerConnection)
                .where(
                        fileServerConnection.user.id.eq(userId),
                        file.hasPersonalInfo.isTrue(),
                        file.riskLevel.eq(RiskLevel.HIGH)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public long sumFileSizeWithPersonalInfo(Long userId) {
        Long sum = queryFactory
                .select(file.fileSize.sum())
                .from(file)
                .join(file.connection, fileServerConnection)
                .where(
                        fileServerConnection.user.id.eq(userId),
                        file.hasPersonalInfo.isTrue()
                )
                .fetchOne();
        return sum != null ? sum : 0L;
    }

    private BooleanExpression connectionIdEq(Long connectionId) {
        return connectionId != null ? fileServerConnection.id.eq(connectionId) : null;
    }

    private BooleanExpression fileCategoryEq(String fileCategory) {
        if (fileCategory == null || fileCategory.isBlank()) {
            return null;
        }
        return fileType.type.eq(FileCategory.valueOf(fileCategory));
    }

    private BooleanExpression maskedEq(Boolean masked) {
        if (masked == null) {
            return null;
        }

        var totalPiisSubQuery = JPAExpressions
                .select(filePii.totalPiisCount.sum().coalesce(0))
                .from(filePii)
                .where(filePii.file.id.eq(file.id));

        var maskedPiisSubQuery = JPAExpressions
                .select(filePii.maskedPiisCount.sum().coalesce(0))
                .from(filePii)
                .where(filePii.file.id.eq(file.id));

        if (masked) {
            // 완전 마스킹: maskedPiis == totalPiis && totalPiis > 0
            return totalPiisSubQuery.gt(0)
                    .and(maskedPiisSubQuery.eq(totalPiisSubQuery));
        } else {
            // 미마스킹 또는 부분: maskedPiis < totalPiis
            return maskedPiisSubQuery.lt(totalPiisSubQuery);
        }
    }

    private BooleanExpression riskLevelEq(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            return null;
        }
        return file.riskLevel.eq(RiskLevel.valueOf(riskLevel));
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return fileServerConnection.connectionName.containsIgnoreCase(keyword)
                .or(file.name.containsIgnoreCase(keyword))
                .or(file.filePath.containsIgnoreCase(keyword));
    }
}
