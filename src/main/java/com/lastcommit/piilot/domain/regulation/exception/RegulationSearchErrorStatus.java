package com.lastcommit.piilot.domain.regulation.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum RegulationSearchErrorStatus implements ErrorReason {

    EMPTY_QUERY(HttpStatus.BAD_REQUEST, "REG4001", "검색어를 입력해주세요."),
    QUERY_TOO_LONG(HttpStatus.BAD_REQUEST, "REG4002", "검색어는 500자를 초과할 수 없습니다."),

    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REG5001", "AI 서버 연결에 실패했습니다."),
    AI_SERVER_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "REG5002", "AI 서버 응답 시간이 초과되었습니다."),
    AI_SERVER_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "REG5003", "AI 서버 응답이 비어있습니다."),
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REG5004", "AI 응답 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    RegulationSearchErrorStatus(HttpStatus httpStatus, String code, String message) {
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
