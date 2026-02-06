package com.lastcommit.piilot.domain.chatbot.controller;

import com.lastcommit.piilot.domain.chatbot.docs.ChatbotControllerDocs;
import com.lastcommit.piilot.domain.chatbot.dto.request.ChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.ChatbotResponseDTO;
import com.lastcommit.piilot.domain.chatbot.service.ChatbotService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController implements ChatbotControllerDocs {

    private final ChatbotService chatbotService;

    @Override
    @PostMapping
    public CommonResponse<ChatbotResponseDTO> chat(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatbotRequestDTO request
    ) {
        ChatbotResponseDTO result = chatbotService.chat(userId, request);
        return CommonResponse.onSuccess(result);
    }
}
