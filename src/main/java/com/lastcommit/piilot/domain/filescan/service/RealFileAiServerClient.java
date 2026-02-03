package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FileScanAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanAiResponseDTO;
import com.lastcommit.piilot.domain.filescan.exception.FileScanErrorStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@Profile("!local")
public class RealFileAiServerClient implements FileAiServerClient {

    private static final String SCAN_ENDPOINT = "/api/ai/file/scan";
    // 파일 스캔은 @Async로 별도 스레드 풀에서 실행되므로 메인 요청 스레드에 영향 없음
    // 여러 파일을 배치로 처리하므로 마스킹(5분)보다 긴 타임아웃 필요
    // isScanning 플래그로 연결당 동시 스캔 1개로 제한되어 스레드 풀 고갈 위험 낮음
    private static final Duration TIMEOUT = Duration.ofMinutes(30);

    private final WebClient webClient;

    public RealFileAiServerClient(WebClient.Builder webClientBuilder,
                                   @Value("${ai-server.base-url:http://localhost:8000}") String aiServerBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(aiServerBaseUrl).build();
    }

    @Override
    public FileScanAiResponseDTO scanFiles(FileScanAiRequestDTO request) {
        log.info("Sending batch scan request to AI server: {} files for connectionId={}",
                request.piiFiles().size(), request.connectionId());

        if (request.piiFiles().isEmpty()) {
            log.info("No files to scan");
            return new FileScanAiResponseDTO(List.of());
        }

        try {
            FileScanAiResponseDTO response = webClient.post()
                    .uri(SCAN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FileScanAiResponseDTO.class)
                    .timeout(TIMEOUT)
                    .block();

            log.info("AI server returned {} results",
                    response != null && response.results() != null ? response.results().size() : 0);

            return response != null ? response : new FileScanAiResponseDTO(List.of());
        } catch (Exception e) {
            log.error("AI server call failed: {}", e.getMessage());
            throw new GeneralException(FileScanErrorStatus.AI_SERVER_CONNECTION_FAILED);
        }
    }
}
