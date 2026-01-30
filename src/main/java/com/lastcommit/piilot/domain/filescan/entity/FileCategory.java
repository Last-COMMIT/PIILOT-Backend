package com.lastcommit.piilot.domain.filescan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileCategory {
    DOCUMENT("문서"),
    PHOTO("사진"),
    AUDIO("오디오"),
    VIDEO("영상");

    private final String displayName;
}
