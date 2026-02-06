package com.lastcommit.piilot.domain.chatbot.client;

import com.lastcommit.piilot.domain.chatbot.dto.request.AiChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.AiChatbotResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("local")
public class StubChatbotAiClient implements ChatbotAiClient {

    @Override
    public AiChatbotResponseDTO chat(AiChatbotRequestDTO request) {
        log.info("[STUB] AI 서버 챗봇 요청: question={}", request.question());

        String answer = "안녕하세요! PIILOT 챗봇입니다. 질문해주신 내용에 대해 답변드리겠습니다.\n\n" +
                "질문: " + request.question() + "\n\n" +
                "이 질문에 대한 답변은 실제 AI 서버와 연동되어 제공됩니다. " +
                "현재는 로컬 테스트 환경이므로 더미 응답을 반환하고 있습니다.";

        List<String> sources = List.of(
                "더미 출처 1",
                "더미 출처 2"
        );

        log.info("[STUB] AI 서버 챗봇 응답 완료");

        return new AiChatbotResponseDTO(answer, sources);
    }
}
