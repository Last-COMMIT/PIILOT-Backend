package com.lastcommit.piilot.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 환경변수에서 쉼표로 구분된 문자열을 리스트로 설정합니다.
     * 예: CORS_ALLOWED_ORIGINS=https://piilot.com,https://www.piilot.com
     *
     * @param origins 쉼표로 구분된 출처 문자열 또는 리스트
     */
    public void setAllowedOrigins(Object origins) {
        if (origins instanceof String originsString) {
            if (originsString.isBlank()) {
                this.allowedOrigins = new ArrayList<>();
            } else {
                this.allowedOrigins = Arrays.stream(originsString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        } else if (origins instanceof List<?> originsList) {
            this.allowedOrigins = originsList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
    }
}
