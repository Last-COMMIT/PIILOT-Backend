package com.lastcommit.piilot.domain.chatbot.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum ChatbotErrorStatus implements ErrorReason {

    EMPTY_QUESTION(HttpStatus.BAD_REQUEST, "CHAT4001", "질문을 입력해주세요."),
    QUESTION_TOO_LONG(HttpStatus.BAD_REQUEST, "CHAT4002", "질문은 1000자를 초과할 수 없습니다."),

    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT5001", "AI 서버 연결에 실패했습니다."),
    AI_SERVER_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT5002", "AI 서버 응답 시간이 초과되었습니다."),
    AI_SERVER_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT5003", "AI 서버 응답이 비어있습니다."),
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT5004", "AI 응답 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ChatbotErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
