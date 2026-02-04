package com.lastcommit.piilot.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FILE_ISSUE_DETECTED("파일 개인정보 이슈 탐지"),
    DB_ISSUE_DETECTED("DB 개인정보 이슈 탐지"),
    FILE_SCAN_COMPLETED("파일 스캔 완료"),
    DB_SCAN_COMPLETED("DB 스캔 완료"),
    MASKING_COMPLETED("마스킹 처리 완료");

    private final String displayName;
}
