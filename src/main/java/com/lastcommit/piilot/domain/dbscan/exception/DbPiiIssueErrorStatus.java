package com.lastcommit.piilot.domain.dbscan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DbPiiIssueErrorStatus implements ErrorReason {

    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "DBISSUE4041", "이슈를 찾을 수 없습니다."),
    ISSUE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DBISSUE4031", "해당 이슈에 대한 접근 권한이 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "DBISSUE4001", "유효하지 않은 작업 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DbPiiIssueErrorStatus(HttpStatus httpStatus, String code, String message) {
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
