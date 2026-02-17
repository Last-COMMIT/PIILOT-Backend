package com.lastcommit.piilot.domain.filescan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 파일 스캔 비동기 실행기.
 * <p>
 * Spring @Async는 같은 클래스 내 자기호출(self-invocation) 시 프록시를 거치지 않아 무시됩니다.
 * DB 스캔의 DbTableAsyncScanner처럼 별도 빈으로 분리하여 @Async가 정상 동작하도록 합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileScanAsyncExecutor {

    private final FileScanService fileScanService;

    @Async("fileScanExecutor")
    public void executeScanAsync(Long connectionId, Long scanHistoryId) {
        log.info("Starting async file scan for connectionId={}", connectionId);

        try {
            fileScanService.executeScan(connectionId, scanHistoryId);
        } catch (Exception e) {
            log.error("File scan failed for connectionId={}: {}", connectionId, e.getMessage(), e);
            fileScanService.markScanFailed(scanHistoryId);
        } finally {
            fileScanService.markScanStopped(connectionId);
        }
    }
}
