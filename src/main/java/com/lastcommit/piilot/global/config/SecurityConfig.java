package com.lastcommit.piilot.global.config;

import com.lastcommit.piilot.global.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    @PostConstruct
    public void logCorsConfig() {
        log.info("CORS allowed origins: {}", corsProperties.getAllowedOrigins());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        // ADMIN 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경별 허용 출처 (application-{profile}.yml에서 설정)
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 요청 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 브라우저에 노출할 응답 헤더
        configuration.setExposedHeaders(List.of("Authorization"));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}