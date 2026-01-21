package com.lastcommit.piilot.global.error.status;

import org.springframework.http.HttpStatus;

public interface ErrorReason {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
