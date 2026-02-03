package com.lastcommit.piilot.domain.filescan.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FilePiiErrorStatus implements ErrorReason {

    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FILEPII4041", "파일 서버 연결 정보를 찾을 수 없습니다."),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FILEPII4031", "해당 연결에 대한 접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
