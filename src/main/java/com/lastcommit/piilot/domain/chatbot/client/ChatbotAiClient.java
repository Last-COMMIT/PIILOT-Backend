package com.lastcommit.piilot.domain.chatbot.client;

import com.lastcommit.piilot.domain.chatbot.dto.request.AiChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.AiChatbotResponseDTO;

public interface ChatbotAiClient {

    AiChatbotResponseDTO chat(AiChatbotRequestDTO request);
}
