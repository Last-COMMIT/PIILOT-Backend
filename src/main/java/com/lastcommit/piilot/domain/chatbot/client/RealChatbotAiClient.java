package com.lastcommit.piilot.domain.chatbot.client;

import com.lastcommit.piilot.domain.chatbot.dto.request.AiChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.AiChatbotResponseDTO;
import com.lastcommit.piilot.domain.chatbot.exception.ChatbotErrorStatus;
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
public class RealChatbotAiClient implements ChatbotAiClient {

    private static final String CHATBOT_PATH = "/api/ai/chat/chatbot";

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

        log.info("RealChatbotAiClient initialized with baseUrl={}, timeout={}s",
                aiServerProperties.baseUrl(), timeoutSeconds);
    }

    @Override
    public AiChatbotResponseDTO chat(AiChatbotRequestDTO request) {
        log.info("AI 서버 챗봇 요청: question={}", request.question());

        try {
            AiChatbotResponseDTO response = webClient.post()
                    .uri(CHATBOT_PATH)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiChatbotResponseDTO.class)
                    .block();

            if (response == null) {
                throw new GeneralException(ChatbotErrorStatus.AI_SERVER_EMPTY_RESPONSE);
            }

            int sourceCount = response.sources() != null ? response.sources().size() : 0;
            log.info("AI 서버 챗봇 응답 완료: {} 출처", sourceCount);
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 챗봇 요청 실패 - HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(ChatbotErrorStatus.AI_SERVER_ERROR);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 챗봇 요청 실패: {}", e.getMessage(), e);
            throw new GeneralException(ChatbotErrorStatus.AI_SERVER_ERROR);
        }
    }
}
