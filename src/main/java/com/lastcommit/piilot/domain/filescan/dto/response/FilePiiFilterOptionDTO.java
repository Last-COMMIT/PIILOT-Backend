package com.lastcommit.piilot.domain.filescan.dto.response;

public record FilePiiFilterOptionDTO(
        Object id,
        String name
) {
    public static FilePiiFilterOptionDTO of(Long id, String name) {
        return new FilePiiFilterOptionDTO(id, name);
    }

    public static FilePiiFilterOptionDTO of(String code, String name) {
        return new FilePiiFilterOptionDTO(code, name);
    }
}
