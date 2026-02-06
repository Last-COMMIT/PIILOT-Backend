package com.lastcommit.piilot.domain.dbscan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DbConnectionErrorStatus implements ErrorReason {

    DBMS_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "DBCONN4041", "DBMS 유형을 찾을 수 없습니다."),
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "DBCONN4042", "DB 연결 정보를 찾을 수 없습니다."),
    CONNECTION_NAME_DUPLICATE(HttpStatus.CONFLICT, "DBCONN4091", "이미 존재하는 연결 이름입니다."),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DBCONN4031", "해당 연결에 대한 접근 권한이 없습니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "DBCONN4002", "비밀번호는 필수입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DbConnectionErrorStatus(HttpStatus httpStatus, String code, String message) {
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
