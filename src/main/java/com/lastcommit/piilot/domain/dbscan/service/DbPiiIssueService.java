package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.internal.DbPiiIssueStatsDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.*;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import com.lastcommit.piilot.domain.dbscan.exception.DbPiiErrorStatus;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiIssueRepository;
import com.lastcommit.piilot.domain.shared.UserStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbPiiIssueService {

    private final DbPiiIssueRepository issueRepository;
    private final UnencryptedDataFetcher unencryptedDataFetcher;
    private final AesEncryptor aesEncryptor;

    public DbPiiIssueListResponseDTO getIssueList(Long userId, Pageable pageable) {
        // 통계 계산
        DbPiiIssueStatsDTO stats = issueRepository.calculateStats(userId);
        DbPiiIssueStatsResponseDTO statsResponse = DbPiiIssueStatsResponseDTO.from(stats);

        // ACTIVE 이슈 전체 조회 (fetch join)
        List<DbPiiIssue> allIssues = issueRepository.findActiveIssuesWithDetails(userId);

        // 테이블별 그룹화
        Map<Long, List<DbPiiIssue>> groupedByTable = allIssues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getDbPiiColumn().getDbTable().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 이슈 수 기준 내림차순 정렬
        List<Map.Entry<Long, List<DbPiiIssue>>> sortedEntries = groupedByTable.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .toList();

        // 페이지네이션 적용 (테이블 그룹 단위)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedEntries.size());

        List<DbPiiIssueTableGroupResponseDTO> pageContent;
        boolean hasNext;

        if (start >= sortedEntries.size()) {
            pageContent = Collections.emptyList();
            hasNext = false;
        } else {
            List<Map.Entry<Long, List<DbPiiIssue>>> pageEntries = sortedEntries.subList(start, end);
            pageContent = pageEntries.stream()
                    .map(entry -> {
                        List<DbPiiIssue> issues = entry.getValue();
                        DbTable table = issues.get(0).getDbPiiColumn().getDbTable();
                        return DbPiiIssueTableGroupResponseDTO.of(table, issues);
                    })
                    .toList();
            hasNext = end < sortedEntries.size();
        }

        Slice<DbPiiIssueTableGroupResponseDTO> content = new SliceImpl<>(pageContent, pageable, hasNext);

        return DbPiiIssueListResponseDTO.of(statsResponse, content);
    }

    public DbPiiIssueDetailResponseDTO getIssueDetail(Long userId, Long issueId) {
        DbPiiIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new GeneralException(DbPiiErrorStatus.ISSUE_NOT_FOUND));

        // 권한 검증
        validateIssueAccess(userId, issue);

        // 비암호화 데이터 조회
        List<UnencryptedRecordDTO> unencryptedRecords = fetchUnencryptedRecords(issue);

        return DbPiiIssueDetailResponseDTO.of(issue, unencryptedRecords);
    }

    @Transactional
    public DbPiiIssueStatusUpdateResponseDTO updateIssueStatus(Long userId, Long issueId, UserStatus userStatus) {
        DbPiiIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new GeneralException(DbPiiErrorStatus.ISSUE_NOT_FOUND));

        // 권한 검증
        validateIssueAccess(userId, issue);

        // 상태 변경
        issue.updateUserStatus(userStatus);

        return DbPiiIssueStatusUpdateResponseDTO.from(issue);
    }

    private void validateIssueAccess(Long userId, DbPiiIssue issue) {
        Long ownerId = issue.getDbPiiColumn()
                .getDbTable()
                .getDbServerConnection()
                .getUser()
                .getId();

        if (!ownerId.equals(userId)) {
            throw new GeneralException(DbPiiErrorStatus.ISSUE_ACCESS_DENIED);
        }
    }

    private List<UnencryptedRecordDTO> fetchUnencryptedRecords(DbPiiIssue issue) {
        try {
            DbPiiColumn column = issue.getDbPiiColumn();
            DbTable table = column.getDbTable();
            DbServerConnection connection = table.getDbServerConnection();

            String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());

            return unencryptedDataFetcher.fetch(connection, table, column, decryptedPassword);
        } catch (Exception e) {
            log.error("Failed to fetch unencrypted records for issue {}: {}", issue.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
