package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.entity.FileScanHistory;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileScanHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 파일 스캔 비동기 실행기.
 * <p>
 * Spring @Async는 같은 클래스 내 자기호출(self-invocation) 시 프록시를 거치지 않아 무시됩니다.
 * DB 스캔의 DbTableAsyncScanner처럼 별도 빈으로 분리하여 @Async가 정상 동작하도록 합니다.
 * </p>
 * <p>
 * FileScanService와의 순환 의존성을 피하기 위해 ApplicationContext를 통해 지연 조회합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileScanAsyncExecutor {

    private final ApplicationContext applicationContext;
    private final FileServerConnectionRepository connectionRepository;
    private final FileScanHistoryRepository scanHistoryRepository;

    @Async("fileScanExecutor")
    public void executeScanAsync(Long connectionId, Long scanHistoryId) {
        log.info("Starting async file scan for connectionId={}", connectionId);

        // self-invocation 시 @Transactional 프록시를 거치지 않으므로
        // ApplicationContext를 통해 프록시 빈을 조회하여 호출
        FileScanAsyncExecutor self = applicationContext.getBean(FileScanAsyncExecutor.class);

        try {
            FileScanService fileScanService = applicationContext.getBean(FileScanService.class);
            fileScanService.executeScan(connectionId, scanHistoryId);
        } catch (Exception e) {
            log.error("File scan failed for connectionId={}: {}", connectionId, e.getMessage(), e);
            self.markScanFailed(scanHistoryId);
        } finally {
            self.markScanStopped(connectionId);
        }
    }

    @Transactional
    public void markScanStopped(Long connectionId) {
        connectionRepository.findById(connectionId)
                .ifPresent(FileServerConnection::stopScanning);
    }

    @Transactional
    public void markScanFailed(Long scanHistoryId) {
        scanHistoryRepository.findById(scanHistoryId)
                .ifPresent(history -> history.updateFailed(LocalDateTime.now()));
    }
}
