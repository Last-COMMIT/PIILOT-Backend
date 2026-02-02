package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiIssueStatsDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.exception.FilePiiErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiIssueRepository;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiRepository;
import com.lastcommit.piilot.domain.shared.UserStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
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
public class FilePiiIssueService {

    private final FilePiiIssueRepository issueRepository;
    private final FilePiiRepository filePiiRepository;

    public FilePiiIssueListResponseDTO getIssueList(Long userId, Pageable pageable) {
        // 통계 계산
        FilePiiIssueStatsDTO stats = issueRepository.calculateStats(userId);
        FilePiiIssueStatsResponseDTO statsResponse = FilePiiIssueStatsResponseDTO.from(stats);

        // ACTIVE 이슈 전체 조회 (fetch join)
        List<FilePiiIssue> allIssues = issueRepository.findActiveIssuesWithDetails(userId);

        // 파일 서버(Connection)별 그룹화
        Map<Long, List<FilePiiIssue>> groupedByConnection = allIssues.stream()
                .collect(Collectors.groupingBy(
                        issue -> issue.getConnection().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 이슈 수 기준 내림차순 정렬
        List<Map.Entry<Long, List<FilePiiIssue>>> sortedEntries = groupedByConnection.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .toList();

        // 페이지네이션 적용 (서버 그룹 단위)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedEntries.size());

        List<FilePiiIssueServerGroupResponseDTO> pageContent;
        boolean hasNext;

        if (start >= sortedEntries.size()) {
            pageContent = Collections.emptyList();
            hasNext = false;
        } else {
            List<Map.Entry<Long, List<FilePiiIssue>>> pageEntries = sortedEntries.subList(start, end);
            pageContent = pageEntries.stream()
                    .map(entry -> {
                        List<FilePiiIssue> issues = entry.getValue();
                        FileServerConnection connection = issues.get(0).getConnection();

                        // 각 이슈에 대해 FilePii 정보 조회
                        List<FilePiiIssueResponseDTO> issueDTOs = issues.stream()
                                .map(issue -> {
                                    List<FilePii> filePiis = filePiiRepository.findByFileIdWithPiiType(
                                            issue.getFile().getId()
                                    );
                                    return FilePiiIssueResponseDTO.from(issue, filePiis);
                                })
                                .toList();

                        return FilePiiIssueServerGroupResponseDTO.of(connection, issueDTOs);
                    })
                    .toList();
            hasNext = end < sortedEntries.size();
        }

        Slice<FilePiiIssueServerGroupResponseDTO> content = new SliceImpl<>(pageContent, pageable, hasNext);

        return FilePiiIssueListResponseDTO.of(statsResponse, content);
    }

    public FilePiiIssueDetailResponseDTO getIssueDetail(Long userId, Long issueId) {
        FilePiiIssue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> new GeneralException(FilePiiErrorStatus.ISSUE_NOT_FOUND));

        // 권한 검증
        validateIssueAccess(userId, issue);

        // 해당 파일의 FilePii 목록 조회
        List<FilePii> filePiis = filePiiRepository.findByFileIdWithPiiType(issue.getFile().getId());

        return FilePiiIssueDetailResponseDTO.of(issue, filePiis);
    }

    @Transactional
    public FilePiiIssueStatusUpdateResponseDTO updateIssueStatus(Long userId, Long issueId, UserStatus userStatus) {
        FilePiiIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new GeneralException(FilePiiErrorStatus.ISSUE_NOT_FOUND));

        // 권한 검증
        validateIssueAccess(userId, issue);

        // 상태 변경
        issue.updateUserStatus(userStatus);

        return FilePiiIssueStatusUpdateResponseDTO.from(issue);
    }

    private void validateIssueAccess(Long userId, FilePiiIssue issue) {
        Long ownerId = issue.getConnection().getUser().getId();

        if (!ownerId.equals(userId)) {
            throw new GeneralException(FilePiiErrorStatus.ISSUE_ACCESS_DENIED);
        }
    }
}
