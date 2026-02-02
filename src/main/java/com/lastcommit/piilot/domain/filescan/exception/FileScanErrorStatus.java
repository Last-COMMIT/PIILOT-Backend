package com.lastcommit.piilot.domain.filescan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum FileScanErrorStatus implements ErrorReason {

    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FILESCAN4041", "파일 서버 연결 정보를 찾을 수 없습니다."),
    CONNECTION_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "FILESCAN4001", "연결되지 않은 파일 서버입니다."),
    SCAN_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "FILESCAN4091", "이미 스캔이 진행 중입니다."),
    SCAN_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "FILESCAN4042", "스캔 이력을 찾을 수 없습니다."),
    FILE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILESCAN4043", "지원하지 않는 파일 유형입니다."),
    PII_TYPE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "FILESCAN5001", "PII 유형을 찾을 수 없습니다."),
    FILE_SCAN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILESCAN5002", "파일 스캔에 실패했습니다."),
    AI_SERVER_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "FILESCAN5031", "AI 서버 연결에 실패했습니다."),
    AI_SERVER_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "FILESCAN5032", "AI 서버 응답이 비어있습니다."),
    UNSUPPORTED_SERVER_TYPE(HttpStatus.BAD_REQUEST, "FILESCAN4002", "지원하지 않는 서버 유형입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FileScanErrorStatus(HttpStatus httpStatus, String code, String message) {
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
