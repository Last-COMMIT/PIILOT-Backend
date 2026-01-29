package com.lastcommit.piilot.domain.dbscan.dto.response;

public record UnencryptedSampleDTO(
        String key,     // 레코드 키 (user_id 등)
        String value    // 실제 값 (이메일 등) - 마스킹 처리
) {}
