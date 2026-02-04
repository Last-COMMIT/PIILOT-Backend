package com.lastcommit.piilot.domain.document.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DocumentErrorStatus implements ErrorReason {

    // 400 Bad Request
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "DOC4001", "파일이 비어있습니다."),
    INVALID_DOCUMENT_TYPE(HttpStatus.BAD_REQUEST, "DOC4002", "유효하지 않은 파일 유형입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "DOC4003", "파일 크기가 50MB를 초과합니다."),

    // 404 Not Found
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOC4041", "문서를 찾을 수 없습니다."),

    // 500 Internal Server Error
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5001", "파일 저장에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DocumentErrorStatus(HttpStatus httpStatus, String code, String message) {
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
