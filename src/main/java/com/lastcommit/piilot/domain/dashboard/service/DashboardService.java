package com.lastcommit.piilot.domain.dashboard.service;

import com.lastcommit.piilot.domain.dashboard.dto.response.*;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiColumnRepository;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiIssueRepository;
import com.lastcommit.piilot.domain.dbscan.repository.DbServerConnectionRepository;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiIssueRepository;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DbServerConnectionRepository dbConnectionRepository;
    private final FileServerConnectionRepository fileConnectionRepository;
    private final DbPiiColumnRepository dbPiiColumnRepository;
    private final FileRepository fileRepository;
    private final FilePiiRepository filePiiRepository;
    private final DbPiiIssueRepository dbPiiIssueRepository;
    private final FilePiiIssueRepository filePiiIssueRepository;

    public DashboardSummaryResponseDTO getSummary(Long userId) {
        DashboardStatsDTO stats = getStats(userId);
        List<PiiDistributionDTO> piiDistribution = getPiiDistribution(userId);
        List<RecentDbIssueDTO> recentDbIssues = getRecentDbIssues(userId);
        List<RecentFileIssueDTO> recentFileIssues = getRecentFileIssues(userId);

        return new DashboardSummaryResponseDTO(stats, piiDistribution, recentDbIssues, recentFileIssues);
    }

    public DashboardTrendResponseDTO getTrends(Long userId) {
        List<MonthlyIssueTrendDTO> dbTrend = getMonthlyDbIssueTrend(userId);
        List<MonthlyIssueTrendDTO> fileTrend = getMonthlyFileIssueTrend(userId);

        return new DashboardTrendResponseDTO(dbTrend, fileTrend);
    }

    private DashboardStatsDTO getStats(Long userId) {
        // 연결 수
        long dbCount = dbConnectionRepository.countByUserId(userId);
        long fileCount = fileConnectionRepository.countByUserId(userId);

        // DB PII 통계
        long piiColumnCount = dbPiiColumnRepository.countByUserId(userId);
        long totalRecords = dbPiiColumnRepository.sumTotalRecordsByUserId(userId);
        long encRecords = dbPiiColumnRepository.sumEncRecordsByUserId(userId);
        double columnEncRate = totalRecords > 0 ? (encRecords * 100.0 / totalRecords) : 0.0;

        // 파일 PII 통계
        long piiFileCount = fileRepository.countPiiFilesByUserId(userId);
        long encPiiFileCount = fileRepository.countEncryptedPiiFilesByUserId(userId);
        double fileEncRate = piiFileCount > 0 ? (encPiiFileCount * 100.0 / piiFileCount) : 0.0;

        // 이슈 수
        long dbIssueCount = dbPiiIssueRepository.countByUserIdAndIssueStatus(userId, IssueStatus.ACTIVE);
        long fileIssueCount = filePiiIssueRepository.countByUserIdAndIssueStatus(userId, IssueStatus.ACTIVE);

        return DashboardStatsDTO.of(
                dbCount, fileCount,
                piiColumnCount, columnEncRate,
                piiFileCount, fileEncRate,
                dbIssueCount, fileIssueCount
        );
    }

    private List<PiiDistributionDTO> getPiiDistribution(Long userId) {
        // DB PII 분포
        List<Object[]> dbDistribution = dbPiiColumnRepository.getPiiDistributionByUserId(userId);
        // 파일 PII 분포
        List<Object[]> fileDistribution = filePiiRepository.getPiiDistributionByUserId(userId);

        // 합산
        Map<PiiCategory, Long> combinedMap = new EnumMap<>(PiiCategory.class);

        for (Object[] row : dbDistribution) {
            PiiCategory type = (PiiCategory) row[0];
            Long count = ((Number) row[1]).longValue();
            combinedMap.merge(type, count, Long::sum);
        }

        for (Object[] row : fileDistribution) {
            PiiCategory type = (PiiCategory) row[0];
            Long count = ((Number) row[1]).longValue();
            combinedMap.merge(type, count, Long::sum);
        }

        long totalCount = combinedMap.values().stream().mapToLong(Long::longValue).sum();

        // DTO 변환 및 정렬 (count 내림차순)
        return combinedMap.entrySet().stream()
                .map(entry -> PiiDistributionDTO.of(
                        entry.getKey().name(),
                        entry.getKey().getDisplayName(),
                        entry.getValue(),
                        totalCount
                ))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .collect(Collectors.toList());
    }

    private List<RecentDbIssueDTO> getRecentDbIssues(Long userId) {
        List<DbPiiIssue> issues = dbPiiIssueRepository.findTop4ByUserIdAndStatusOrderByDetectedAtDesc(
                userId, IssueStatus.ACTIVE);

        return issues.stream()
                .map(RecentDbIssueDTO::from)
                .collect(Collectors.toList());
    }

    private List<RecentFileIssueDTO> getRecentFileIssues(Long userId) {
        List<FilePiiIssue> issues = filePiiIssueRepository.findTop4ByUserIdAndStatusOrderByDetectedAtDesc(
                userId, IssueStatus.ACTIVE);

        // 각 이슈에 대해 FilePii 조회
        return issues.stream()
                .map(issue -> {
                    List<FilePii> filePiis = filePiiRepository.findByFileIdWithPiiType(issue.getFile().getId());
                    return RecentFileIssueDTO.from(issue, filePiis);
                })
                .collect(Collectors.toList());
    }

    private List<MonthlyIssueTrendDTO> getMonthlyDbIssueTrend(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> result = dbPiiIssueRepository.countByLast12Months(userId, startDate);
        return buildMonthlyTrend(result);
    }

    private List<MonthlyIssueTrendDTO> getMonthlyFileIssueTrend(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> result = filePiiIssueRepository.countByLast12Months(userId, startDate);
        return buildMonthlyTrend(result);
    }

    private List<MonthlyIssueTrendDTO> buildMonthlyTrend(List<Object[]> queryResult) {
        // 쿼리 결과를 Map으로 변환
        Map<String, Long> yearMonthCountMap = new HashMap<>();
        for (Object[] row : queryResult) {
            String yearMonth = (String) row[0];
            long count = ((Number) row[1]).longValue();
            yearMonthCountMap.put(yearMonth, count);
        }

        // 최근 12개월 목록 생성 (데이터 없으면 0)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth current = YearMonth.now();

        List<MonthlyIssueTrendDTO> result = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String yearMonth = ym.format(formatter);
            long count = yearMonthCountMap.getOrDefault(yearMonth, 0L);
            result.add(MonthlyIssueTrendDTO.of(yearMonth, count));
        }

        return result;
    }
}
