package com.lastcommit.piilot.global.auth;

import com.lastcommit.piilot.global.auth.dto.response.TokenResponseDTO;
import com.lastcommit.piilot.global.config.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(

                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenValidity =
                jwtProperties.accessTokenValidity();
        this.refreshTokenValidity =
                jwtProperties.refreshTokenValidity();
    }

    // 토큰 생성
    public TokenResponseDTO createToken(Long userId, String email,
                                        String role) {
        Date now = new Date();
        Date accessTokenExpiry = new Date(now.getTime() +
                accessTokenValidity);
        Date refreshTokenExpiry = new Date(now.getTime() +
                refreshTokenValidity);

        String accessToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(accessTokenExpiry)
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(refreshTokenExpiry)
                .signWith(secretKey)
                .compact();

        return new TokenResponseDTO(accessToken, refreshToken,
                accessTokenValidity);
    }

    // 토큰에서 Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 토큰에서 role 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e; // 만료된 토큰은 별도 처리 위해 재throw
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
