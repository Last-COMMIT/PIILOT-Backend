package com.lastcommit.piilot.domain.chatbot.dto.request;

public record AiChatbotRequestDTO(
        String question
) {
    public static AiChatbotRequestDTO of(String question) {
        return new AiChatbotRequestDTO(question);
    }
}
