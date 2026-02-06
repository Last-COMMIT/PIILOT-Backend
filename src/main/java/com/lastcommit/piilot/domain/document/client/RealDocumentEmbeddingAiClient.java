package com.lastcommit.piilot.domain.document.client;

import com.lastcommit.piilot.domain.document.dto.request.AiEmbeddingRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.AiEmbeddingResponseDTO;
import com.lastcommit.piilot.domain.document.entity.DocumentType;
import com.lastcommit.piilot.domain.document.exception.DocumentErrorStatus;
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
@Profile("!stub")
@RequiredArgsConstructor
public class RealDocumentEmbeddingAiClient implements DocumentEmbeddingAiClient {

    private static final String REGULATION_UPLOAD_PATH = "/api/ai/chat/upload-regulations";
    private static final String DB_DICTIONARY_UPLOAD_PATH = "/api/ai/db/upload-column-dictionary";

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
                .defaultHeader("ngrok-skip-browser-warning", "true")
                .build();

        log.info("RealDocumentEmbeddingAiClient initialized with baseUrl={}, timeout={}s",
                aiServerProperties.baseUrl(), timeoutSeconds);
    }

    @Override
    public AiEmbeddingResponseDTO requestEmbedding(AiEmbeddingRequestDTO request, DocumentType documentType) {
        String path = getPathByDocumentType(documentType);
        log.info("AI 서버 임베딩 요청 - documentType={}, path={}, filePath={}",
                documentType, path, request.filePath());

        try {
            AiEmbeddingResponseDTO response = webClient.post()
                    .uri(path)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiEmbeddingResponseDTO.class)
                    .block();

            if (response == null) {
                throw new GeneralException(DocumentErrorStatus.AI_SERVER_EMPTY_RESPONSE);
            }

            log.info("AI 서버 임베딩 완료 - success={}, message={}", response.success(), response.message());
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 임베딩 실패 - HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(DocumentErrorStatus.AI_SERVER_ERROR);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 임베딩 실패: {}", e.getMessage(), e);
            throw new GeneralException(DocumentErrorStatus.AI_SERVER_ERROR);
        }
    }

    private String getPathByDocumentType(DocumentType documentType) {
        return switch (documentType) {
            case REGULATION -> REGULATION_UPLOAD_PATH;
            case DB_DICTIONARY -> DB_DICTIONARY_UPLOAD_PATH;
        };
    }
}
