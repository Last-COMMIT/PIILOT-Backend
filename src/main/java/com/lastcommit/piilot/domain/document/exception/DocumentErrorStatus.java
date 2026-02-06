package com.lastcommit.piilot.domain.document.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import org.springframework.http.HttpStatus;

public enum DocumentErrorStatus implements ErrorReason {

    // 4xx Client Errors
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "DOC4001", "유효하지 않은 파일명입니다."),
    INVALID_DOCUMENT_TYPE(HttpStatus.BAD_REQUEST, "DOC4002", "유효하지 않은 문서 타입입니다."),
    INVALID_S3_URL(HttpStatus.BAD_REQUEST, "DOC4003", "유효하지 않은 S3 URL입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "DOC4041", "사용자를 찾을 수 없습니다."),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOC4042", "문서를 찾을 수 없습니다."),

    // 5xx Server Errors
    S3_PRESIGNED_URL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5001", "Presigned URL 생성에 실패했습니다."),
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5002", "S3 업로드에 실패했습니다."),

    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5011", "AI 서버 연결에 실패했습니다."),
    AI_SERVER_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5012", "AI 서버 응답 시간이 초과되었습니다."),
    AI_SERVER_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "DOC5013", "AI 서버 응답이 비어있습니다.");

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
