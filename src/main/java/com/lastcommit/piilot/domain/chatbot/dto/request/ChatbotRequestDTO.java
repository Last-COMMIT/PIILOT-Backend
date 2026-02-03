package com.lastcommit.piilot.domain.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotRequestDTO(
        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 1000, message = "질문은 1000자를 초과할 수 없습니다.")
        String question
) {
}
