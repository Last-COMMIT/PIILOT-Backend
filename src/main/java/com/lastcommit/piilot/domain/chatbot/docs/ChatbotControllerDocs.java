package com.lastcommit.piilot.domain.chatbot.docs;

import com.lastcommit.piilot.domain.chatbot.dto.request.ChatbotRequestDTO;
import com.lastcommit.piilot.domain.chatbot.dto.response.ChatbotResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Chatbot", description = "챗봇 API")
@SecurityRequirement(name = "JWT TOKEN")
public interface ChatbotControllerDocs {

    @Operation(summary = "챗봇 질문하기", description = "AI 챗봇에게 질문하고 답변을 받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "질문 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (질문 누락 또는 길이 초과)"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    CommonResponse<ChatbotResponseDTO> chat(
            @Parameter(hidden = true) Long userId,
            ChatbotRequestDTO request
    );
}
