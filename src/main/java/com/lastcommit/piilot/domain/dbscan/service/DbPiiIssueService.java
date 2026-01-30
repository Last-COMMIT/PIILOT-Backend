package com.lastcommit.piilot.domain.dbscan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiIssueStatusUpdateRequest;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueStatusResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiIssueTableResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.UnencryptedSampleDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiIssue;
import com.lastcommit.piilot.domain.dbscan.exception.DbPiiIssueErrorStatus;
import com.lastcommit.piilot.domain.dbscan.repository.DbPiiIssueRepository;
import com.lastcommit.piilot.domain.shared.UserStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbPiiIssueService {

    private final DbPiiIssueRepository dbPiiIssueRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_SAMPLE_COUNT = 10;

    /**
     * 이슈 요약 통계 조회
     */
    public DbPiiIssueSummaryResponseDTO getSummary(Long userId) {
        return dbPiiIssueRepository.getSummary(userId);
    }

    /**
     * 테이블별 이슈 목록 조회
     */
    public Slice<DbPiiIssueTableResponseDTO> getIssueList(Long userId, Pageable pageable) {
        return dbPiiIssueRepository.getIssuesByTable(userId, pageable);
    }

    /**
     * 이슈 작업 상태 변경
     */
    @Transactional
    public DbPiiIssueStatusResponseDTO updateStatus(Long userId, Long issueId, DbPiiIssueStatusUpdateRequest request) {
        DbPiiIssue issue = dbPiiIssueRepository.findByIdWithDetails(issueId, userId)
                .orElseThrow(() -> new GeneralException(DbPiiIssueErrorStatus.ISSUE_NOT_FOUND));

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(request.userStatus());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(DbPiiIssueErrorStatus.INVALID_USER_STATUS);
        }

        issue.updateUserStatus(newStatus);

        return DbPiiIssueStatusResponseDTO.from(issue);
    }

    /**
     * 이슈 상세 조회
     */
    public DbPiiIssueDetailResponseDTO getIssueDetail(Long userId, Long issueId) {
        DbPiiIssue issue = dbPiiIssueRepository.findByIdWithDetails(issueId, userId)
                .orElseThrow(() -> new GeneralException(DbPiiIssueErrorStatus.ISSUE_NOT_FOUND));

        List<UnencryptedSampleDTO> samples = parseUnencryptedSamples(
                issue.getDbPiiColumn().getUnencRecordsKey()
        );

        return DbPiiIssueDetailResponseDTO.from(issue, samples);
    }

    /**
     * unencRecordsKey JSON을 파싱하여 샘플 데이터 생성
     * 현재는 키만 있고 실제 값은 마스킹 처리
     */
    private List<UnencryptedSampleDTO> parseUnencryptedSamples(String unencRecordsKey) {
        if (unencRecordsKey == null || unencRecordsKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            List<Object> keys = objectMapper.readValue(unencRecordsKey, new TypeReference<>() {});
            return keys.stream()
                    .limit(MAX_SAMPLE_COUNT)
                    .map(key -> new UnencryptedSampleDTO(
                            String.valueOf(key),
                            "****"  // 마스킹 처리 (실제 값은 원본 DB 조회 필요)
                    ))
                    .toList();
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
