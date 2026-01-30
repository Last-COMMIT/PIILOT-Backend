package com.lastcommit.piilot.domain.regulation.dto.response;

import java.util.List;

public record PrincipleDTO(
        String title,
        List<String> items
) {
}
