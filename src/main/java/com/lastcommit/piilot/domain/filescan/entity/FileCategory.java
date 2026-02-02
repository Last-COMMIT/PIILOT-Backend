package com.lastcommit.piilot.domain.filescan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileCategory {
    DOCUMENT("문서"),  // 문서 (pdf, docx, xlsx 등)
    PHOTO("사진"),     // 사진 (jpg, png 등)
    AUDIO("음성"),     // 오디오 (mp3, wav 등)
    VIDEO("영상");     // 비디오 (mp4, avi 등)

    private final String displayName;
}
