package com.lastcommit.piilot.domain.filescan.scheduler;

import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import com.lastcommit.piilot.domain.filescan.service.FileScanService;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileScanScheduler {

    private final FileServerConnectionRepository connectionRepository;
    private final FileScanService fileScanService;

    @Scheduled(cron = "0 30 2 * * *")
    public void scheduleDailyScan() {
        log.info("Starting scheduled daily file scan");
        List<FileServerConnection> connections = connectionRepository.findByStatus(ConnectionStatus.CONNECTED);

        for (FileServerConnection connection : connections) {
            try {
                fileScanService.startScan(connection.getId());
                log.info("Scheduled scan started for connectionId={}", connection.getId());
            } catch (Exception e) {
                log.error("Scheduled scan failed to start for connectionId={}: {}",
                        connection.getId(), e.getMessage());
            }
        }
        log.info("Scheduled daily file scan finished. Total connections processed: {}", connections.size());
    }
}
