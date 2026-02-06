package com.lastcommit.piilot.domain.filescan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum FileMaskingErrorStatus implements ErrorReason {

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MASKING4041", "파일을 찾을 수 없습니다."),
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MASKING4042", "커넥션을 찾을 수 없습니다."),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MASKING4031", "파일 접근 권한이 없습니다."),
    NOT_MASKING_TARGET(HttpStatus.BAD_REQUEST, "MASKING4001", "마스킹 대상 파일이 아닙니다."),
    PREVIEW_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "MASKING4002", "미리보기를 지원하지 않는 파일입니다."),
    MASKING_RESULT_EXPIRED(HttpStatus.BAD_REQUEST, "MASKING4003", "마스킹 결과가 만료되었습니다. 다시 마스킹해주세요."),
    FILE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MASKING5001", "파일 다운로드에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MASKING5002", "파일 업로드에 실패했습니다."),
    MASKING_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MASKING5003", "마스킹 처리에 실패했습니다."),
    AI_SERVER_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "MASKING5031", "AI 서버 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FileMaskingErrorStatus(HttpStatus httpStatus, String code, String message) {
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
