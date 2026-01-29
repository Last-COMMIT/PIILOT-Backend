package com.lastcommit.piilot.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PiiCategory {
    NM("이름"),
    RRN("주민등록번호"),
    ADD("주소"),
    IP("IP주소"),
    PH("전화번호"),
    ACN("계좌번호"),
    PP("여권번호"),
    EM("이메일"),
    FACE("얼굴");

    private final String displayName;
}
