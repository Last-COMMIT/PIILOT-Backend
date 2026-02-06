package com.lastcommit.piilot.global.error.status;

import org.springframework.http.HttpStatus;

public enum CommonErrorStatus implements ErrorReason{

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // JSON 관련
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "JSON4001", "JSON 파싱 중 에러가 발생했습니다."),

    // ExceptionAdvice 관련
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "ARGUMENT4001", "Argument Validation을 실패했습니다."),
    TYPE_OR_FORMAT_NOT_VALID(HttpStatus.BAD_REQUEST, "ARGUMENT4002", "Argument의 타입이나 형식이 올바르지 않습니다."),
    CONSTRAINTS_VIOLATION_EXCEPTION_ERROR(HttpStatus.BAD_REQUEST, "ARGUMENT4003", "ConstraintsViolationException 추출 도중 에러 발생"),

    // 페이지네이션 관련
    INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "PAGE4001", "페이지 번호는 0 이상이어야 합니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PAGE4002", "페이지 크기는 1 이상 100 이하여야 합니다."),

    // 인증 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4041", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH4091", "이미 존재하는 이메일입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "AUTH4001", "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH4011", "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "만료된 토큰입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    //  enum 생성자 (필수)
    CommonErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    //  ErrorReason 구현 메서드들
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
