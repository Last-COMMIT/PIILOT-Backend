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
import org.springframework.stereotype.Service;
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

    @Transactional
    public DbScanResponseDTO scanConnection(Long connectionId) {
        // 1. Connection 조회 + CONNECTED 검증
        DbServerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new GeneralException(DbConnectionErrorStatus.CONNECTION_NOT_FOUND));

        if (connection.getStatus() != ConnectionStatus.CONNECTED) {
            throw new GeneralException(DbScanErrorStatus.CONNECTION_NOT_CONNECTED);
        }

        // 2. ScanHistory 생성 (IN_PROGRESS)
        DbScanHistory scanHistory = DbScanHistory.builder()
                .dbServerConnection(connection)
                .status(ScanStatus.IN_PROGRESS)
                .scanStartTime(LocalDateTime.now())
                .totalTablesCount(0L)
                .totalColumnsCount(0L)
                .scannedColumnsCount(0L)
                .build();
        scanHistory = dbScanHistoryRepository.save(scanHistory);

        // 3. 1단계: 스키마 스캔
        String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());
        List<SchemaTableInfoDTO> schemaInfos;
        try {
            schemaInfos = dbSchemaScanner.scanSchema(connection, decryptedPassword);
        } catch (Exception e) {
            log.error("Schema scan failed for connectionId={}: {}", connectionId, e.getMessage());
            throw new GeneralException(DbScanErrorStatus.SCHEMA_SCAN_FAILED);
        }

        // 4. 1단계 저장: db_tables 저장/갱신
        LocalDateTime now = LocalDateTime.now();
        long totalColumnsCount = 0L;
        Map<String, DbTable> tableMap = new HashMap<>();

        for (SchemaTableInfoDTO schemaInfo : schemaInfos) {
            totalColumnsCount += schemaInfo.columnCount();

            Optional<DbTable> existingTable = dbTableRepository
                    .findByDbServerConnectionIdAndName(connectionId, schemaInfo.tableName());

            DbTable dbTable;
            if (existingTable.isPresent()) {
                dbTable = existingTable.get();
                dbTable.updateScanInfo(schemaInfo.columnCount(), schemaInfo.changeSignature(), now);
            } else {
                dbTable = DbTable.builder()
                        .dbServerConnection(connection)
                        .name(schemaInfo.tableName())
                        .totalColumns(schemaInfo.columnCount())
                        .lastChangeSignature(schemaInfo.changeSignature())
                        .lastScannedAt(now)
                        .build();
                dbTable = dbTableRepository.save(dbTable);
            }
            tableMap.put(schemaInfo.tableName(), dbTable);
        }

        // 5. 2단계: AI 서버에 PII 컬럼 식별 요청
        PiiIdentificationRequestDTO piiRequest = buildPiiIdentificationRequest(schemaInfos);
        PiiIdentificationResponseDTO piiResponse;
        try {
            piiResponse = aiServerClient.identifyPiiColumns(piiRequest);
        } catch (Exception e) {
            log.error("PII identification failed for connectionId={}: {}", connectionId, e.getMessage());
            throw new GeneralException(DbScanErrorStatus.PII_IDENTIFICATION_FAILED);
        }

        // 6. 2단계 저장: db_pii_columns 병합
        List<DbPiiColumn> existingPiiColumns = dbPiiColumnRepository.findByDbTableDbServerConnectionId(connectionId);
        Map<String, DbPiiColumn> existingPiiMap = existingPiiColumns.stream()
                .collect(Collectors.toMap(
                        col -> col.getDbTable().getName() + ":" + col.getName() + ":" + col.getPiiType().getType().name(),
                        col -> col
                ));

        Set<String> newPiiKeys = new HashSet<>();
        List<DbPiiColumn> activePiiColumns = new ArrayList<>();

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
                activePiiColumns.add(existingCol);
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
                newCol = dbPiiColumnRepository.save(newCol);
                activePiiColumns.add(newCol);
            }
        }

        // 사라진 PII 컬럼 삭제 + 관련 ACTIVE 이슈 해결
        for (Map.Entry<String, DbPiiColumn> entry : existingPiiMap.entrySet()) {
            if (!newPiiKeys.contains(entry.getKey())) {
                DbPiiColumn removedCol = entry.getValue();
                resolveActiveIssue(removedCol, now);
                dbPiiColumnRepository.delete(removedCol);
            }
        }

        // 7. 3단계: AI 서버에 암호화 확인 요청
        EncryptionCheckRequestDTO encRequest = buildEncryptionCheckRequest(connectionId, piiResponse);
        EncryptionCheckResponseDTO encResponse;
        try {
            encResponse = aiServerClient.checkEncryption(encRequest);
        } catch (Exception e) {
            log.error("Encryption check failed for connectionId={}: {}", connectionId, e.getMessage());
            throw new GeneralException(DbScanErrorStatus.ENCRYPTION_CHECK_FAILED);
        }

        // 8. 3단계 저장: db_pii_columns 암호화 결과 업데이트
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
                        unencKeysJson
                );
            }
        }

        // 9. 4단계: risk_level 계산 + ScanHistory 완료
        long scannedColumnsCount = 0L;
        for (DbPiiColumn piiColumn : activePiiColumns) {
            RiskLevel riskLevel = calculateRiskLevel(piiColumn);
            piiColumn.updateRiskLevel(riskLevel);
            scannedColumnsCount++;
        }

        scanHistory.updateCompleted(LocalDateTime.now(), ScanStatus.COMPLETED,
                (long) schemaInfos.size(), totalColumnsCount, scannedColumnsCount);

        // 10. 5단계: 이슈 생성/해결
        for (DbPiiColumn piiColumn : activePiiColumns) {
            processIssue(piiColumn, connection, now);
        }

        return DbScanResponseDTO.from(scanHistory);
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

    private void processIssue(DbPiiColumn piiColumn, DbServerConnection connection, LocalDateTime now) {
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
        } else if (isFullyEncrypted && Boolean.TRUE.equals(piiColumn.getIsIssueOpen())) {
            // 완전 암호화 + 이슈 오픈 → 기존 ACTIVE 이슈 해결
            resolveActiveIssue(piiColumn, now);
            piiColumn.closeIssue();
        }
    }

    private void resolveActiveIssue(DbPiiColumn piiColumn, LocalDateTime now) {
        dbPiiIssueRepository.findByDbPiiColumnIdAndIssueStatus(piiColumn.getId(), IssueStatus.ACTIVE)
                .ifPresent(issue -> issue.resolve(now));
    }

    private String convertToJson(List<Long> keys) {
        try {
            return objectMapper.writeValueAsString(keys);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert unenc keys to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}
