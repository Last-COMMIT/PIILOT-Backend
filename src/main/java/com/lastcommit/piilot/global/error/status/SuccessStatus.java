package com.lastcommit.piilot.global.error.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {
    _OK(HttpStatus.OK, "COMMON200", "요청이 성공했습니다."),
    _CREATED(HttpStatus.CREATED, "COMMON201", "리소스를 생성했습니다."),
    _ACCEPTED(HttpStatus.ACCEPTED, "COMMON202", "요청이 접수되었습니다."),
            ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
