package com.lastcommit.piilot.domain.chatbot.service;

import com.lastcommit.piilot.domain.chatbot.client.ChatbotAiClient;
import com.lastcommit.piilot.domain.chatbot.dto.request.AiChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.request.ChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.AiChatbotResponseDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.ChatbotResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotAiClient aiClient;

    public ChatbotResponseDTO chat(Long userId, ChatbotRequestDTO request) {
        log.info("챗봇 질문 요청: userId={}, question={}", userId, request.question());

        AiChatbotRequestDTO aiRequest = AiChatbotRequestDTO.of(request.question());
        AiChatbotResponseDTO aiResponse = aiClient.chat(aiRequest);

        ChatbotResponseDTO response = ChatbotResponseDTO.from(aiResponse);

        int sourceCount = response.sources() != null ? response.sources().size() : 0;
        log.info("챗봇 질문 완료: userId={}, sourcesCount={}", userId, sourceCount);
        return response;
    }
}
