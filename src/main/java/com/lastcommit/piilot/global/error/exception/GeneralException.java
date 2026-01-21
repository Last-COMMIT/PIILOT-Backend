package com.lastcommit.piilot.global.error.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final ErrorReason errorReason;
}