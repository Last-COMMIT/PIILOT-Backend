package com.lastcommit.piilot.domain.document.entity;

import lombok.Getter;

@Getter
public enum DocumentType {
    LAWS("법령"),
    INTERNAL_REGULATIONS("내규"),
    DB_INFO("DB 사전");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }
}
