package com.lastcommit.piilot.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    // 사용자 레벨 이슈 작업 트래킹 상태
    ISSUE("미완료"),
    RUNNING("진행중"),
    DONE("완료");

    private final String displayName;

    /**
     * 다음 상태로 전환 (순환: ISSUE -> RUNNING -> DONE -> ISSUE)
     */
    public UserStatus next() {
        return switch (this) {
            case ISSUE -> RUNNING;
            case RUNNING -> DONE;
            case DONE -> ISSUE;
        };
    }
}
