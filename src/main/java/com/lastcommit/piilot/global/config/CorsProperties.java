package com.lastcommit.piilot.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * CORS 허용 출처를 설정합니다.
     * YAML 리스트 또는 환경변수(쉼표 구분)를 Spring Boot가 자동 변환합니다.
     * 예: CORS_ALLOWED_ORIGINS=https://piilot.com,https://www.piilot.com
     */
    public void setAllowedOrigins(List<String> origins) {
        if (origins != null) {
            this.allowedOrigins = new ArrayList<>(origins);
        }
    }
}
