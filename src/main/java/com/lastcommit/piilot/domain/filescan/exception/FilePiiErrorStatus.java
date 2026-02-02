package com.lastcommit.piilot.domain.filescan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum FilePiiErrorStatus implements ErrorReason {

    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FILEPII4041", "파일 서버 연결 정보를 찾을 수 없습니다."),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FILEPII4031", "해당 연결에 대한 접근 권한이 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILEPII4042", "파일을 찾을 수 없습니다."),
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILEPII4043", "이슈를 찾을 수 없습니다."),
    ISSUE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FILEPII4032", "해당 이슈에 대한 접근 권한이 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "FILEPII4002", "유효하지 않은 작업 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FilePiiErrorStatus(HttpStatus httpStatus, String code, String message) {
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
