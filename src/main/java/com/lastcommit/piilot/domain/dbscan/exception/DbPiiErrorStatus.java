package com.lastcommit.piilot.domain.dbscan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DbPiiErrorStatus implements ErrorReason {

    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "DBPII4041", "DB 연결 정보를 찾을 수 없습니다."),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DBPII4031", "해당 연결에 대한 접근 권한이 없습니다."),
    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "DBPII4042", "테이블을 찾을 수 없습니다."),
    TABLE_CONNECTION_MISMATCH(HttpStatus.BAD_REQUEST, "DBPII4001", "테이블이 지정된 커넥션에 속하지 않습니다."),

    // 이슈 관련 에러 코드
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "DBPII4043", "이슈를 찾을 수 없습니다."),
    ISSUE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DBPII4032", "해당 이슈에 대한 접근 권한이 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "DBPII4002", "유효하지 않은 작업 상태입니다."),
    UNENCRYPTED_DATA_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DBPII5001", "비암호화 데이터 조회에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DbPiiErrorStatus(HttpStatus httpStatus, String code, String message) {
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
