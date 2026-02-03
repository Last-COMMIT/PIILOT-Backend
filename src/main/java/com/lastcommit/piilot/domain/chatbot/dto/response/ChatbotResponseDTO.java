package com.lastcommit.piilot.domain.chatbot.dto.response;

import java.util.Collections;
import java.util.List;

public record ChatbotResponseDTO(
        String answer,
        List<String> sources
) {
    public static ChatbotResponseDTO from(AiChatbotResponseDTO aiResponse) {
        List<String> sources = aiResponse.sources() != null
                ? aiResponse.sources()
                : Collections.emptyList();

        return new ChatbotResponseDTO(
                aiResponse.answer(),
                sources
        );
    }
}
