package com.lastcommit.piilot.domain.chatbot.dto.response;

import java.util.List;

public record AiChatbotResponseDTO(
        String answer,
        List<String> sources
) {
}
