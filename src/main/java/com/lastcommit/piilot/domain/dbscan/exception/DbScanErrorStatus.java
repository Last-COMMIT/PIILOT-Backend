package com.lastcommit.piilot.domain.dbscan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DbScanErrorStatus implements ErrorReason {

    CONNECTION_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "DBSCAN4001", "연결되지 않은 DB입니다."),
    SCHEMA_SCAN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DBSCAN5001", "스키마 스캔에 실패했습니다."),
    PII_IDENTIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DBSCAN5002", "PII 식별에 실패했습니다."),
    ENCRYPTION_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DBSCAN5003", "암호화 확인에 실패했습니다."),
    PII_TYPE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DBSCAN5004", "PII 유형을 찾을 수 없습니다."),
    SCAN_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "DBSCAN4091", "이미 스캔이 진행 중입니다."),
    AI_SERVER_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "DBSCAN5031", "AI 서버 연결에 실패했습니다."),
    AI_SERVER_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "DBSCAN5032", "AI 서버 응답이 비어있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DbScanErrorStatus(HttpStatus httpStatus, String code, String message) {
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
