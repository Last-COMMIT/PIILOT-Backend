package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.request.EncryptionCheckRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.PiiIdentificationRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;
import com.lastcommit.piilot.domain.dbscan.exception.DbScanErrorStatus;
import com.lastcommit.piilot.global.config.AiServerProperties;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class RealAiServerClient implements AiServerClient {

    private static final String PII_IDENTIFICATION_PATH = "/api/ai/db/detect-pii-columns";
    private static final String ENCRYPTION_CHECK_PATH = "/api/ai/db/check-encryption";

    private final AiServerProperties aiServerProperties;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        int timeoutSeconds = aiServerProperties.timeoutSeconds();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds))
                                .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds)));

        this.webClient = WebClient.builder()
                .baseUrl(aiServerProperties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("RealAiServerClient initialized with baseUrl={}, timeout={}s",
                aiServerProperties.baseUrl(), timeoutSeconds);
    }

    @Override
    public PiiIdentificationResponseDTO identifyPiiColumns(PiiIdentificationRequestDTO request) {
        log.info("AI 서버 PII 컬럼 식별 요청: {} 테이블", request.tables().size());

        try {
            PiiIdentificationResponseDTO response = webClient.post()
                    .uri(PII_IDENTIFICATION_PATH)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PiiIdentificationResponseDTO.class)
                    .block();

            if (response == null) {
                throw new GeneralException(DbScanErrorStatus.AI_SERVER_EMPTY_RESPONSE);
            }

            log.info("AI 서버 PII 컬럼 식별 완료: {} 컬럼 탐지", response.piiColumns().size());
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 PII 식별 실패 - HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(DbScanErrorStatus.PII_IDENTIFICATION_FAILED);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 PII 식별 실패: {}", e.getMessage(), e);
            throw new GeneralException(DbScanErrorStatus.AI_SERVER_CONNECTION_FAILED);
        }
    }

    @Override
    public EncryptionCheckResponseDTO checkEncryption(EncryptionCheckRequestDTO request) {
        log.info("AI 서버 암호화 확인 요청: connectionId={}, {} 컬럼",
                request.connectionId(), request.piiColumns().size());

        try {
            EncryptionCheckResponseDTO response = webClient.post()
                    .uri(ENCRYPTION_CHECK_PATH)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EncryptionCheckResponseDTO.class)
                    .block();

            if (response == null) {
                throw new GeneralException(DbScanErrorStatus.AI_SERVER_EMPTY_RESPONSE);
            }

            log.info("AI 서버 암호화 확인 완료: {} 결과", response.results().size());
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 암호화 확인 실패 - HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(DbScanErrorStatus.ENCRYPTION_CHECK_FAILED);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 암호화 확인 실패: {}", e.getMessage(), e);
            throw new GeneralException(DbScanErrorStatus.AI_SERVER_CONNECTION_FAILED);
        }
    }
}
