package com.lastcommit.piilot.domain.notification.exception;

import com.lastcommit.piilot.global.error.status.ErrorReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorStatus implements ErrorReason {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIF4041", "알림을 찾을 수 없습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTIF4031", "알림에 접근할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
