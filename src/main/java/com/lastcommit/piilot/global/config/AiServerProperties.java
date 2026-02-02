package com.lastcommit.piilot.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-server")
public record AiServerProperties(
        String baseUrl,
        int timeoutSeconds
) {
}
