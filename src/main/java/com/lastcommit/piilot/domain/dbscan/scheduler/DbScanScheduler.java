package com.lastcommit.piilot.domain.dbscan.scheduler;

import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.repository.DbServerConnectionRepository;
import com.lastcommit.piilot.domain.dbscan.service.DbScanService;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbScanScheduler {

    private final DbServerConnectionRepository connectionRepository;
    private final DbScanService dbScanService;

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleDailyScan() {
        log.info("Starting scheduled daily DB scan");
        List<DbServerConnection> connections = connectionRepository.findByStatus(ConnectionStatus.CONNECTED);

        for (DbServerConnection connection : connections) {
            try {
                dbScanService.scanConnection(connection.getId());
                log.info("Scheduled scan completed for connectionId={}", connection.getId());
            } catch (Exception e) {
                log.error("Scheduled scan failed for connectionId={}: {}", connection.getId(), e.getMessage());
            }
        }
        log.info("Scheduled daily DB scan finished. Total connections processed: {}", connections.size());
    }
}
