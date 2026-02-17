package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.internal.FileMetadataDTO;
import com.lastcommit.piilot.domain.filescan.dto.request.FileScanAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanAiResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanStatusResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.*;
import com.lastcommit.piilot.domain.filescan.exception.FileScanErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.*;
import com.lastcommit.piilot.domain.shared.*;
import com.lastcommit.piilot.domain.shared.repository.PiiTypeRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileScanService {

    private final FileServerConnectionRepository connectionRepository;
    private final FileScanHistoryRepository scanHistoryRepository;
    private final FileRepository fileRepository;
    private final FileTypeRepository fileTypeRepository;
    private final FilePiiRepository filePiiRepository;
    private final FilePiiIssueRepository filePiiIssueRepository;
    private final PiiTypeRepository piiTypeRepository;
    private final FileSchemaScanner fileSchemaScanner;
    private final FileEncryptionChecker fileEncryptionChecker;
    private final FileAiServerClient fileAiServerClient;
    private final AesEncryptor aesEncryptor;
    private final FileScanAsyncExecutor fileScanAsyncExecutor;

    @Transactional
    public FileScanResponseDTO startScan(Long connectionId) {
        // 1. Connection validation
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FileScanErrorStatus.CONNECTION_NOT_FOUND));

        if (connection.getStatus() != ConnectionStatus.CONNECTED) {
            throw new GeneralException(FileScanErrorStatus.CONNECTION_NOT_CONNECTED);
        }

        // 2. Check for existing in-progress scan (isScanning 필드로 확인)
        if (Boolean.TRUE.equals(connection.getIsScanning())) {
            throw new GeneralException(FileScanErrorStatus.SCAN_ALREADY_IN_PROGRESS);
        }

        // 3. Mark connection as scanning
        connection.startScanning();

        // 5. Create ScanHistory (IN_PROGRESS)
        FileScanHistory scanHistory = FileScanHistory.builder()
                .fileServerConnection(connection)
                .status(ScanStatus.IN_PROGRESS)
                .scanStartTime(LocalDateTime.now())
                .totalFilesCount(0L)
                .totalFilesSize(0L)
                .scannedFilesCount(0L)
                .build();
        scanHistory = scanHistoryRepository.save(scanHistory);

        // 6. Start async scan (별도 빈을 통해 호출해야 @Async 프록시가 동작함)
        fileScanAsyncExecutor.executeScanAsync(connectionId, scanHistory.getId());

        return FileScanResponseDTO.from(scanHistory);
    }

    // executeScanAsync는 FileScanAsyncExecutor로 이동됨
    // (같은 클래스 내 @Async 자기호출은 Spring 프록시를 거치지 않아 동기 실행됨)

    @Transactional
    public void markScanStopped(Long connectionId) {
        connectionRepository.findById(connectionId)
                .ifPresent(FileServerConnection::stopScanning);
    }

    @Transactional
    public void executeScan(Long connectionId, Long scanHistoryId) {
        FileServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(FileScanErrorStatus.CONNECTION_NOT_FOUND));

        FileScanHistory scanHistory = scanHistoryRepository.findById(scanHistoryId)
                .orElseThrow(() -> new GeneralException(FileScanErrorStatus.SCAN_HISTORY_NOT_FOUND));

        String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());
        LocalDateTime now = LocalDateTime.now();

        // Step 1: Collect file metadata from server
        log.info("Step 1: Collecting file metadata for connectionId={}", connectionId);
        List<FileMetadataDTO> fileMetadataList = fileSchemaScanner.scanFiles(connection, decryptedPassword);
        log.info("Step 1 completed: {} files collected", fileMetadataList.size());

        long totalFilesSize = 0L;
        List<File> filesToScan = new ArrayList<>();
        Map<String, File> filePathMap = new HashMap<>();

        // Step 2: Save/update file metadata and determine scan targets
        log.info("Step 2: Processing files and checking encryption");
        for (FileMetadataDTO metadata : fileMetadataList) {
            totalFilesSize += metadata.fileSize();

            Optional<FileType> fileTypeOpt = fileTypeRepository.findByExtension(metadata.extension());
            if (fileTypeOpt.isEmpty()) {
                log.debug("Unsupported file type: {}", metadata.extension());
                continue;
            }
            FileType fileType = fileTypeOpt.get();

            Optional<File> existingFile = fileRepository.findByConnectionIdAndFilePath(
                    connectionId, metadata.filePath());

            File file;
            if (existingFile.isPresent()) {
                file = existingFile.get();
                file.updateMetadata(metadata.fileSize(), metadata.lastModifiedTime());
            } else {
                // Check encryption for new files
                boolean isEncrypted = fileEncryptionChecker.isEncrypted(
                        connection, decryptedPassword,
                        metadata.filePath(), metadata.extension(), metadata.fileSize());

                file = File.builder()
                        .connection(connection)
                        .fileType(fileType)
                        .name(metadata.fileName())
                        .filePath(metadata.filePath())
                        .fileSize(metadata.fileSize())
                        .isEncrypted(isEncrypted)
                        .hasPersonalInfo(false)
                        .lastModifiedTime(metadata.lastModifiedTime())
                        .build();
                file = fileRepository.save(file);
            }

            // Determine if file needs rescan
            if (file.needsRescan(scanHistory.getScanStartTime())) {
                filesToScan.add(file);
                filePathMap.put(file.getFilePath(), file);
            }
        }
        log.info("Step 2 completed: {} files to scan", filesToScan.size());

        // Step 3: AI batch scan
        log.info("Step 3: Running AI batch scan on {} files", filesToScan.size());

        // Filter out encrypted files - AI only scans non-encrypted files
        List<File> nonEncryptedFiles = filesToScan.stream()
                .filter(file -> !Boolean.TRUE.equals(file.getIsEncrypted()))
                .toList();
        log.info("Non-encrypted files to scan: {}", nonEncryptedFiles.size());

        if (!nonEncryptedFiles.isEmpty()) {
            // Build batch request - only file paths as per design document
            List<String> piiFilePaths = nonEncryptedFiles.stream()
                    .map(File::getFilePath)
                    .toList();

            FileScanAiRequestDTO batchRequest = new FileScanAiRequestDTO(
                    String.valueOf(connectionId),
                    piiFilePaths
            );

            // Call AI server
            FileScanAiResponseDTO batchResponse = fileAiServerClient.scanFiles(batchRequest);

            // Process results
            if (batchResponse.results() != null) {
                for (FileScanAiResponseDTO.FileResult result : batchResponse.results()) {
                    File file = filePathMap.get(result.filePath());
                    if (file != null) {
                        processAiResult(file, connection, result, now);
                    }
                }
            }
        }
        log.info("Step 3 completed: AI scan finished");

        // Step 4: Update scan history
        scanHistory.updateCompleted(
                LocalDateTime.now(),
                (long) fileMetadataList.size(),
                totalFilesSize,
                (long) filesToScan.size()
        );

        log.info("Scan completed for connectionId={}: total={}, scanned={}",
                connectionId, fileMetadataList.size(), filesToScan.size());
    }

    private void processAiResult(File file, FileServerConnection connection,
                                  FileScanAiResponseDTO.FileResult result, LocalDateTime now) {
        // Delete existing PII records for this file
        filePiiRepository.deleteByFileId(file.getId());

        // Save new PII records
        boolean piiDetected = Boolean.TRUE.equals(result.piiDetected());
        List<FilePii> newPiis = new ArrayList<>();

        if (result.piiDetails() != null) {
            for (FileScanAiResponseDTO.PiiDetail piiDetail : result.piiDetails()) {
                try {
                    PiiCategory category = PiiCategory.valueOf(piiDetail.piiType());
                    PiiType piiType = piiTypeRepository.findByType(category)
                            .orElse(null);

                    if (piiType != null) {
                        FilePii filePii = FilePii.builder()
                                .file(file)
                                .piiType(piiType)
                                .totalPiisCount(piiDetail.totalCount())
                                .maskedPiisCount(piiDetail.maskedCount())
                                .build();
                        newPiis.add(filePiiRepository.save(filePii));
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown PII type: {}", piiDetail.piiType());
                }
            }
        }

        // Calculate risk level
        RiskLevel riskLevel = calculateRiskLevel(newPiis);

        // Update file
        file.updateScanResult(file.getIsEncrypted(), piiDetected, riskLevel, now);

        // Process issues
        processIssue(file, connection, newPiis, now);
    }

    private RiskLevel calculateRiskLevel(List<FilePii> piiList) {
        if (piiList.isEmpty()) {
            return RiskLevel.LOW;
        }

        double totalWeight = 0.0;
        int totalCount = 0;

        for (FilePii pii : piiList) {
            int unmaskedCount = pii.getTotalPiisCount() - pii.getMaskedPiisCount();
            if (unmaskedCount > 0) {
                float weight = pii.getPiiType().getRiskWeight();
                totalWeight += weight * unmaskedCount;
                totalCount += unmaskedCount;
            }
        }

        if (totalCount == 0) {
            return RiskLevel.LOW;
        }

        double riskScore = totalWeight / totalCount;

        if (riskScore >= 0.7) return RiskLevel.HIGH;
        if (riskScore >= 0.3) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private void processIssue(File file, FileServerConnection connection,
                               List<FilePii> piiList, LocalDateTime now) {
        // Check if there are unmasked PIIs
        boolean hasUnmaskedPii = piiList.stream()
                .anyMatch(pii -> pii.getTotalPiisCount() > pii.getMaskedPiisCount());

        boolean shouldHaveIssue = !Boolean.TRUE.equals(file.getIsEncrypted())
                && Boolean.TRUE.equals(file.getHasPersonalInfo())
                && hasUnmaskedPii;

        if (shouldHaveIssue && !Boolean.TRUE.equals(file.getIsIssueOpen())) {
            // Create new issue
            FilePiiIssue issue = FilePiiIssue.builder()
                    .file(file)
                    .connection(connection)
                    .userStatus(UserStatus.ISSUE)
                    .issueStatus(IssueStatus.ACTIVE)
                    .detectedAt(now)
                    .build();
            filePiiIssueRepository.save(issue);
            file.incrementIssueCount();
        } else if (!shouldHaveIssue && Boolean.TRUE.equals(file.getIsIssueOpen())) {
            // Resolve existing issue
            filePiiIssueRepository.findByFileIdAndIssueStatus(file.getId(), IssueStatus.ACTIVE)
                    .ifPresent(issue -> issue.resolve(now));
            file.closeIssue();
        }
    }

    @Transactional
    public void markScanFailed(Long scanHistoryId) {
        scanHistoryRepository.findById(scanHistoryId)
                .ifPresent(history -> history.updateFailed(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public FileScanStatusResponseDTO getScanStatus(Long connectionId, Long scanHistoryId) {
        FileScanHistory scanHistory = scanHistoryRepository
                .findByIdAndFileServerConnectionId(scanHistoryId, connectionId)
                .orElseThrow(() -> new GeneralException(FileScanErrorStatus.SCAN_HISTORY_NOT_FOUND));

        return FileScanStatusResponseDTO.from(scanHistory);
    }
}
