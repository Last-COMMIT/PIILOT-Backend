package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiIssueStatsDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FilePii;
import com.lastcommit.piilot.domain.filescan.entity.FilePiiIssue;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.exception.FilePiiErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiIssueRepository;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiRepository;
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
public class FilePiiIssueService {

    private static final long MAX_PREVIEW_SIZE = 20 * 1024 * 1024; // 20MB

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // PHOTO
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("heic", "image/heic"),
            // DOCUMENT
            Map.entry("pdf", "application/pdf"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("txt", "text/plain"),
            // AUDIO
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("wav", "audio/wav"),
            // VIDEO
            Map.entry("mp4", "video/mp4"),
            Map.entry("avi", "video/x-msvideo"),
            Map.entry("mov", "video/quicktime")
    );

    private final FilePiiIssueRepository issueRepository;
    private final FilePiiRepository filePiiRepository;
    private final FileDownloader fileDownloader;
    private final AesEncryptor aesEncryptor;

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

        // 파일 미리보기 처리
        File file = issue.getFile();
        FileServerConnection connection = issue.getConnection();
        String extension = file.getFileType().getExtension();
        String mimeType = getMimeType(extension);

        // connection이 null인 경우 미리보기 불가
        if (connection == null) {
            return FilePiiIssueDetailResponseDTO.unavailable(
                    issue, filePiis, extension, mimeType,
                    "파일 서버 연결 정보가 없습니다."
            );
        }

        // 파일 크기 체크
        if (file.getFileSize() != null && file.getFileSize() >= MAX_PREVIEW_SIZE) {
            return FilePiiIssueDetailResponseDTO.unavailable(
                    issue, filePiis, extension, mimeType,
                    "파일 크기가 20MB를 초과하여 미리보기를 지원하지 않습니다."
            );
        }

        // 파일 다운로드 및 base64 인코딩
        try {
            String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());
            byte[] fileContent = fileDownloader.download(connection, decryptedPassword, file.getFilePath());
            String base64Content = Base64.getEncoder().encodeToString(fileContent);

            return FilePiiIssueDetailResponseDTO.available(issue, filePiis, extension, mimeType, base64Content);
        } catch (Exception e) {
            log.error("Failed to download file for preview: issueId={}, filePath={}, error={}",
                    issueId, file.getFilePath(), e.getMessage());
            return FilePiiIssueDetailResponseDTO.unavailable(
                    issue, filePiis, extension, mimeType,
                    "파일 다운로드에 실패했습니다."
            );
        }
    }

    private String getMimeType(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
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
