package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.internal.SchemaTableInfoDTO;
import com.lastcommit.piilot.domain.dbscan.dto.internal.TableScanResultDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.EncryptionCheckRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.PiiIdentificationRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbTableAsyncScanner {

    private final AiServerClient aiServerClient;

    @Async("dbScanExecutor")
    public CompletableFuture<TableScanResultDTO> scanTableAsync(
            Long connectionId,
            SchemaTableInfoDTO tableInfo
    ) {
        String tableName = tableInfo.tableName();
        log.debug("[ASYNC] Starting scan for table: {}", tableName);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 단일 테이블 PII 식별
            PiiIdentificationRequestDTO piiRequest = new PiiIdentificationRequestDTO(
                    List.of(new PiiIdentificationRequestDTO.TableColumns(tableName, tableInfo.columns()))
            );
            PiiIdentificationResponseDTO piiResponse = aiServerClient.identifyPiiColumns(piiRequest);

            List<PiiIdentificationResponseDTO.PiiColumnResult> piiColumns = piiResponse.piiColumns();

            // 2. PII 컬럼이 있으면 암호화 확인
            List<EncryptionCheckResponseDTO.EncryptionResult> encryptionResults;
            if (!piiColumns.isEmpty()) {
                List<EncryptionCheckRequestDTO.PiiColumnInfo> piiColumnInfos = piiColumns.stream()
                        .map(col -> new EncryptionCheckRequestDTO.PiiColumnInfo(
                                col.tableName(), col.columnName(), col.piiType()))
                        .toList();

                EncryptionCheckRequestDTO encRequest = new EncryptionCheckRequestDTO(connectionId, piiColumnInfos);
                EncryptionCheckResponseDTO encResponse = aiServerClient.checkEncryption(encRequest);
                encryptionResults = encResponse.results();
            } else {
                encryptionResults = Collections.emptyList();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[ASYNC] Completed scan for table: {} in {}ms (piiColumns={})",
                    tableName, duration, piiColumns.size());

            return CompletableFuture.completedFuture(
                    new TableScanResultDTO(tableName, piiColumns, encryptionResults)
            );

        } catch (Exception e) {
            log.error("[ASYNC] Failed to scan table {}: {}", tableName, e.getMessage());
            // 실패 시 빈 결과 반환 (개별 테이블 실패가 전체를 중단시키지 않도록)
            return CompletableFuture.completedFuture(
                    new TableScanResultDTO(tableName, Collections.emptyList(), Collections.emptyList())
            );
        }
    }
}
