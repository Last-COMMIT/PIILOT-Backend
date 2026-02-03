package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.MaskingAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.MaskingAiResponseDTO;
import com.lastcommit.piilot.domain.filescan.exception.FileMaskingErrorStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("!local")
public class RealMaskingAiServerClient implements MaskingAiServerClient {

    @Value("${ai-server.base-url:http://localhost:8000}")
    private String aiServerBaseUrl;

    @Value("${ai-server.timeout-seconds:300}")
    private int timeoutSeconds;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024)) // 100MB
                .build();
    }

    @Override
    public MaskingAiResponseDTO maskFile(MaskingAiRequestDTO request) {
        log.info("Calling AI server for file masking: connectionId={}, fileCategory={}, timeout={}s",
                request.connectionId(), request.fileCategory(), timeoutSeconds);

        try {
            MaskingAiResponseDTO response = webClient.post()
                    .uri(aiServerBaseUrl + "/api/ai/file/mask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MaskingAiResponseDTO.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.success())) {
                log.error("AI server returned failure or null response");
                throw new GeneralException(FileMaskingErrorStatus.MASKING_PROCESS_FAILED);
            }

            return response;

        } catch (WebClientResponseException e) {
            log.error("AI server returned error: status={}", e.getStatusCode());
            throw new GeneralException(FileMaskingErrorStatus.AI_SERVER_CONNECTION_FAILED);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call AI server: {}", e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.AI_SERVER_CONNECTION_FAILED);
        }
    }
}
