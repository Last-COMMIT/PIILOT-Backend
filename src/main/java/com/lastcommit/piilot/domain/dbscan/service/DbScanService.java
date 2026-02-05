package com.lastcommit.piilot.domain.dbscan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lastcommit.piilot.domain.dbscan.dto.internal.SchemaTableInfoDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.EncryptionCheckRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.PiiIdentificationRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbScanResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;
import com.lastcommit.piilot.domain.dbscan.entity.*;
import com.lastcommit.piilot.domain.dbscan.exception.DbConnectionErrorStatus;
import com.lastcommit.piilot.domain.dbscan.exception.DbScanErrorStatus;
import com.lastcommit.piilot.domain.dbscan.repository.*;
import com.lastcommit.piilot.domain.shared.*;
import com.lastcommit.piilot.domain.shared.repository.PiiTypeRepository;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbScanService {

    private final DbServerConnectionRepository connectionRepository;
    private final DbTableRepository dbTableRepository;
    private final DbPiiColumnRepository dbPiiColumnRepository;
    private final DbPiiIssueRepository dbPiiIssueRepository;
    private final DbScanHistoryRepository dbScanHistoryRepository;
    private final PiiTypeRepository piiTypeRepository;
    private final DbSchemaScanner dbSchemaScanner;
    private final AiServerClient aiServerClient;
    private final AesEncryptor aesEncryptor;
    private final ObjectMapper objectMapper;

    // 자기 자신 주입 (AOP 프록시를 통한 @Transactional 동작 보장)
    @Lazy
    @Autowired
    private DbScanService self;

    public DbScanResponseDTO scanConnection(Long connectionId) {
        // 1. 스캔 시작 (별도 트랜잭션으로 즉시 커밋)
        self.markScanStarted(connectionId);

        try {
            // 2. 실제 스캔 수행 (별도 트랜잭션)
            return self.executeDbScan(connectionId);
        } finally {
            // 3. 스캔 종료 (별도 트랜잭션으로 즉시 커밋)
            self.markScanStopped(connectionId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markScanStarted(Long connectionId) {
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.CONNECTION_NOT_FOUND));

        if (connection.getStatus() != ConnectionStatus.CONNECTED) {
            throw new GeneralException(DbScanErrorStatus.CONNECTION_NOT_CONNECTED);
        }

        if (Boolean.TRUE.equals(connection.getIsScanning())) {
            throw new GeneralException(DbScanErrorStatus.SCAN_ALREADY_IN_PROGRESS);
        }

        connection.startScanning();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markScanStopped(Long connectionId) {
        connectionRepository.findById(connectionId)
                .ifPresent(DbServerConnection::stopScanning);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DbScanResponseDTO executeDbScan(Long connectionId) {
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.CONNECTION_NOT_FOUND));

        long totalStartTime = System.currentTimeMillis();

        // ScanHistory 생성 (IN_PROGRESS)
        DbScanHistory scanHistory = DbScanHistory.builder()
                .dbServerConnection(connection)
                .status(ScanStatus.IN_PROGRESS)
                .scanStartTime(LocalDateTime.now())
                .totalTablesCount(0L)
                .totalColumnsCount(0L)
                .scannedColumnsCount(0L)
                .build();
        scanHistory = dbScanHistoryRepository.save(scanHistory);

        // ========== 1단계: 스키마 스캔 ==========
        long stage1Start = System.currentTimeMillis();
        String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());
        List<SchemaTableInfoDTO> schemaInfos;
        try {
            schemaInfos = dbSchemaScanner.scanSchema(connection, decryptedPassword);
        } catch (Exception e) {
            log.error("Schema scan failed for connectionId={}: {}", connectionId, e.getMessage());
            throw new GeneralException(DbScanErrorStatus.SCHEMA_SCAN_FAILED);
        }

        // 1단계 저장: db_tables 저장/갱신 + 변경 테이블 필터링 (Phase 2)
        LocalDateTime now = LocalDateTime.now();
        long totalColumnsCount = 0L;
        Map<String, DbTable> tableMap = new HashMap<>();
        List<SchemaTableInfoDTO> changedTables = new ArrayList<>();
        Set<String> unchangedTableNames = new HashSet<>();

        for (SchemaTableInfoDTO schemaInfo : schemaInfos) {
            totalColumnsCount += schemaInfo.columnCount();

            Optional<DbTable> existingTable = dbTableRepository
                    .findByDbServerConnectionIdAndName(connectionId, schemaInfo.tableName());

            DbTable dbTable;
            if (existingTable.isPresent()) {
                dbTable = existingTable.get();
                String oldSignature = dbTable.getLastChangeSignature();
                String newSignature = schemaInfo.changeSignature();

                // 시그니처 비교로 변경 여부 판단
                if (!Objects.equals(oldSignature, newSignature)) {
                    changedTables.add(schemaInfo);
                } else {
                    unchangedTableNames.add(schemaInfo.tableName());
                }

                dbTable.updateScanInfo(schemaInfo.columnCount(), schemaInfo.changeSignature(), now);
            } else {
                // 신규 테이블 → 스캔 대상
                dbTable = DbTable.builder()
                        .dbServerConnection(connection)
                        .name(schemaInfo.tableName())
                        .totalColumns(schemaInfo.columnCount())
                        .lastChangeSignature(schemaInfo.changeSignature())
                        .lastScannedAt(now)
                        .build();
                dbTable = dbTableRepository.save(dbTable);
                changedTables.add(schemaInfo);
            }
            tableMap.put(schemaInfo.tableName(), dbTable);
        }
        int skippedCount = unchangedTableNames.size();
        long stage1Duration = System.currentTimeMillis() - stage1Start;

        // ========== 기존 PII 컬럼 로드 (전체) ==========
        List<DbPiiColumn> existingPiiColumns = dbPiiColumnRepository.findByDbTableDbServerConnectionId(connectionId);
        Map<String, DbPiiColumn> existingPiiMap = existingPiiColumns.stream()
                .collect(Collectors.toMap(
                        col -> col.getDbTable().getName() + ":" + col.getName() + ":" + col.getPiiType().getType().name(),
                        col -> col
                ));

        Set<String> newPiiKeys = new HashSet<>();
        // LinkedHashSet으로 중복 방지 + 순서 유지
        Set<DbPiiColumn> activePiiColumnsSet = new LinkedHashSet<>();

        // 변경되지 않은 테이블의 기존 PII 컬럼은 유지 (Phase 2)
        for (DbPiiColumn existingCol : existingPiiColumns) {
            if (unchangedTableNames.contains(existingCol.getDbTable().getName())) {
                String key = existingCol.getDbTable().getName() + ":" + existingCol.getName() + ":"
                        + existingCol.getPiiType().getType().name();
                newPiiKeys.add(key);
                activePiiColumnsSet.add(existingCol);
            }
        }

        // ========== 2단계: AI 서버에 PII 컬럼 식별 요청 (변경된 테이블만) ==========
        long stage2Start = System.currentTimeMillis();
        PiiIdentificationResponseDTO piiResponse;

        if (changedTables.isEmpty()) {
            // 변경된 테이블 없음 → AI 호출 스킵
            piiResponse = new PiiIdentificationResponseDTO(Collections.emptyList());
            log.info("[PERF] Stage 2 skipped: no changed tables");
        } else {
            PiiIdentificationRequestDTO piiRequest = buildPiiIdentificationRequest(changedTables);
            try {
                piiResponse = aiServerClient.identifyPiiColumns(piiRequest);
            } catch (Exception e) {
                log.error("PII identification failed for connectionId={}: {}", connectionId, e.getMessage());
                throw new GeneralException(DbScanErrorStatus.PII_IDENTIFICATION_FAILED);
            }
        }

        // 2단계 저장: 변경된 테이블의 db_pii_columns 병합
        for (PiiIdentificationResponseDTO.PiiColumnResult result : piiResponse.piiColumns()) {
            DbTable dbTable = tableMap.get(result.tableName());
            if (dbTable == null) continue;

            PiiCategory piiCategory;
            try {
                piiCategory = PiiCategory.valueOf(result.piiType());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown PII type: {}", result.piiType());
                continue;
            }

            String key = result.tableName() + ":" + result.columnName() + ":" + result.piiType();
            newPiiKeys.add(key);

            DbPiiColumn existingCol = existingPiiMap.get(key);
            if (existingCol != null) {
                activePiiColumnsSet.add(existingCol);
            } else {
                PiiType piiType = piiTypeRepository.findByType(piiCategory)
                        .orElseThrow(() -> new GeneralException(DbScanErrorStatus.PII_TYPE_NOT_FOUND));

                DbPiiColumn newCol = DbPiiColumn.builder()
                        .dbTable(dbTable)
                        .piiType(piiType)
                        .name(result.columnName())
                        .isIssueOpen(false)
                        .totalIssuesCount(0)
                        .build();
                // newCol = dbPiiColumnRepository.save(newCol);
                // saveAndFlush로 즉시 영속화하여 이후 DbPiiIssue 생성 시 참조 문제 방지
                newCol = dbPiiColumnRepository.saveAndFlush(newCol);
                activePiiColumnsSet.add(newCol);
            }
        }

        // 사라진 PII 컬럼의 이슈 해결 (변경된 테이블에서만)
        // 주의: 컬럼은 삭제하지 않음 (이슈 FK 참조 유지를 위해)
        Set<String> changedTableNames = changedTables.stream()
                .map(SchemaTableInfoDTO::tableName)
                .collect(Collectors.toSet());

        for (Map.Entry<String, DbPiiColumn> entry : existingPiiMap.entrySet()) {
            DbPiiColumn col = entry.getValue();
            // 변경된 테이블의 PII 컬럼 중 새 스캔에서 발견되지 않은 것 → 이슈만 해결
            if (changedTableNames.contains(col.getDbTable().getName()) && !newPiiKeys.contains(entry.getKey())) {
                resolveActiveIssue(col, now);
                activePiiColumnsSet.remove(col);
            }
        }

        // Set을 List로 변환 (이후 처리용)
        List<DbPiiColumn> activePiiColumns = new ArrayList<>(activePiiColumnsSet);
        long stage2Duration = System.currentTimeMillis() - stage2Start;

        // ========== 3단계: AI 서버에 암호화 확인 요청 (변경된 테이블의 PII만) ==========
        long stage3Start = System.currentTimeMillis();
        EncryptionCheckResponseDTO encResponse;

        // 변경된 테이블에서 발견된 PII 컬럼만 필터링
        List<PiiIdentificationResponseDTO.PiiColumnResult> changedPiiColumns = piiResponse.piiColumns();

        if (changedPiiColumns.isEmpty()) {
            // 변경된 테이블에 PII 없음 → AI 호출 스킵
            encResponse = new EncryptionCheckResponseDTO(Collections.emptyList());
            log.info("[PERF] Stage 3 skipped: no PII columns in changed tables");
        } else {
            EncryptionCheckRequestDTO encRequest = buildEncryptionCheckRequest(connectionId, piiResponse);
            try {
                encResponse = aiServerClient.checkEncryption(encRequest);
            } catch (Exception e) {
                log.error("Encryption check failed for connectionId={}: {}", connectionId, e.getMessage());
                throw new GeneralException(DbScanErrorStatus.ENCRYPTION_CHECK_FAILED);
            }
        }

        // 3단계 저장: db_pii_columns 암호화 결과 업데이트
        Map<String, EncryptionCheckResponseDTO.EncryptionResult> encResultMap = encResponse.results().stream()
                .collect(Collectors.toMap(
                        r -> r.tableName() + ":" + r.columnName() + ":" + r.piiType(),
                        r -> r
                ));

        for (DbPiiColumn piiColumn : activePiiColumns) {
            String key = piiColumn.getDbTable().getName() + ":" + piiColumn.getName() + ":"
                    + piiColumn.getPiiType().getType().name();
            EncryptionCheckResponseDTO.EncryptionResult encResult = encResultMap.get(key);

            if (encResult != null) {
                String unencKeysJson = convertToJson(encResult.unencRecordsKeys());
                piiColumn.updateEncryptionResults(
                        encResult.totalRecordsCount(),
                        encResult.encRecordsCount(),
                        unencKeysJson,
                        encResult.keyColumn()
                );
            }
        }
        long stage3Duration = System.currentTimeMillis() - stage3Start;

        // ========== 4단계: 후처리 (risk_level 계산) ==========
        long stage4Start = System.currentTimeMillis();
        long scannedColumnsCount = 0L;
        for (DbPiiColumn piiColumn : activePiiColumns) {
            RiskLevel riskLevel = calculateRiskLevel(piiColumn);
            piiColumn.updateRiskLevel(riskLevel);
            scannedColumnsCount++;
        }

        scanHistory.updateCompleted(LocalDateTime.now(), ScanStatus.COMPLETED,
                (long) schemaInfos.size(), totalColumnsCount, scannedColumnsCount);
        long stage4Duration = System.currentTimeMillis() - stage4Start;

        // ========== 5단계: 이슈 생성/해결 ==========
        long stage5Start = System.currentTimeMillis();
        int issueCount = 0;
        for (DbPiiColumn piiColumn : activePiiColumns) {
            if (processIssue(piiColumn, connection, now)) {
                issueCount++;
            }
        }
        long stage5Duration = System.currentTimeMillis() - stage5Start;

        // ========== 성능 측정 로그 출력 (Phase 2) ==========
        long totalDuration = System.currentTimeMillis() - totalStartTime;
        logPerformanceMetricsPhase2(connectionId, schemaInfos.size(), changedTables.size(), skippedCount,
                totalColumnsCount, activePiiColumns.size(), issueCount,
                stage1Duration, stage2Duration, stage3Duration, stage4Duration, stage5Duration,
                totalDuration);

        return DbScanResponseDTO.from(scanHistory);
    }

    private void logPerformanceMetricsPhase2(Long connectionId, int tableCount, int changedCount, int skippedCount,
                                              long columnCount, int piiColumnCount, int issueCount,
                                              long stage1, long stage2, long stage3, long stage4, long stage5,
                                              long total) {
        log.info("========== DB SCAN PERFORMANCE (Phase 2: Sync + Incremental Scan) ==========");
        log.info("[PERF] connectionId={}", connectionId);
        log.info("[PERF] tables={}, changedTables={}, skippedTables={}", tableCount, changedCount, skippedCount);
        log.info("[PERF] columns={}, piiColumns={}, issues={}", columnCount, piiColumnCount, issueCount);
        log.info("[PERF] Stage 1 (Schema Scan + Filter): {} ms ({}%)",
                String.format("%6d", stage1), String.format("%5.1f", stage1 * 100.0 / total));
        log.info("[PERF] Stage 2 (PII Identification)  : {} ms ({}%)",
                String.format("%6d", stage2), String.format("%5.1f", stage2 * 100.0 / total));
        log.info("[PERF] Stage 3 (Encryption Check)    : {} ms ({}%)",
                String.format("%6d", stage3), String.format("%5.1f", stage3 * 100.0 / total));
        log.info("[PERF] Stage 4 (Post Processing)     : {} ms ({}%)",
                String.format("%6d", stage4), String.format("%5.1f", stage4 * 100.0 / total));
        log.info("[PERF] Stage 5 (Issue Processing)    : {} ms ({}%)",
                String.format("%6d", stage5), String.format("%5.1f", stage5 * 100.0 / total));
        log.info("[PERF] TOTAL                         : {} ms (100.0%)", String.format("%6d", total));
        log.info("==========================================================================");
    }

    private PiiIdentificationRequestDTO buildPiiIdentificationRequest(List<SchemaTableInfoDTO> schemaInfos) {
        List<PiiIdentificationRequestDTO.TableColumns> tables = schemaInfos.stream()
                .map(info -> new PiiIdentificationRequestDTO.TableColumns(info.tableName(), info.columns()))
                .toList();
        return new PiiIdentificationRequestDTO(tables);
    }

    private EncryptionCheckRequestDTO buildEncryptionCheckRequest(Long connectionId,
                                                                   PiiIdentificationResponseDTO piiResponse) {
        List<EncryptionCheckRequestDTO.PiiColumnInfo> piiColumns = piiResponse.piiColumns().stream()
                .map(col -> new EncryptionCheckRequestDTO.PiiColumnInfo(
                        col.tableName(), col.columnName(), col.piiType()))
                .toList();
        return new EncryptionCheckRequestDTO(connectionId, piiColumns);
    }

    private RiskLevel calculateRiskLevel(DbPiiColumn piiColumn) {
        Long totalRecords = piiColumn.getTotalRecordsCount();
        Long encRecords = piiColumn.getEncRecordsCount();

        if (totalRecords == null || totalRecords == 0) {
            return RiskLevel.LOW;
        }

        long unencCount = totalRecords - (encRecords != null ? encRecords : 0);
        double unencRatio = (double) unencCount / totalRecords;
        double riskScore = piiColumn.getPiiType().getRiskWeight() * unencRatio;

        if (riskScore >= 0.7) return RiskLevel.HIGH;
        if (riskScore >= 0.3) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private boolean processIssue(DbPiiColumn piiColumn, DbServerConnection connection, LocalDateTime now) {
        Long totalRecords = piiColumn.getTotalRecordsCount();
        Long encRecords = piiColumn.getEncRecordsCount();

        boolean hasUnencrypted = totalRecords != null && encRecords != null
                && !totalRecords.equals(encRecords);
        boolean isFullyEncrypted = totalRecords != null && encRecords != null
                && totalRecords.equals(encRecords);

        if (hasUnencrypted && !Boolean.TRUE.equals(piiColumn.getIsIssueOpen())) {
            // 비암호화 존재 + 이슈 미오픈 → 새 이슈 생성
            DbPiiIssue issue = DbPiiIssue.builder()
                    .dbPiiColumn(piiColumn)
                    .connection(connection)
                    .userStatus(UserStatus.ISSUE)
                    .issueStatus(IssueStatus.ACTIVE)
                    .detectedAt(now)
                    .build();
            dbPiiIssueRepository.save(issue);
            piiColumn.incrementIssueCount();
            return true;
        } else if (isFullyEncrypted && Boolean.TRUE.equals(piiColumn.getIsIssueOpen())) {
            // 완전 암호화 + 이슈 오픈 → 기존 ACTIVE 이슈 해결
            resolveActiveIssue(piiColumn, now);
            piiColumn.closeIssue();
        }
        return false;
    }

    private void resolveActiveIssue(DbPiiColumn piiColumn, LocalDateTime now) {
        dbPiiIssueRepository.findByDbPiiColumnIdAndIssueStatus(piiColumn.getId(), IssueStatus.ACTIVE)
                .ifPresent(issue -> issue.resolve(now));
    }

    private String convertToJson(List<String> keys) {
        try {
            return objectMapper.writeValueAsString(keys);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert unenc keys to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}
