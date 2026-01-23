package com.lastcommit.piilot.global.auth.service;

import com.lastcommit.piilot.domain.user.entity.User;
import com.lastcommit.piilot.domain.user.entity.UserRole;
import com.lastcommit.piilot.domain.user.repository.UserRepository;
import com.lastcommit.piilot.global.auth.JwtTokenProvider;
import com.lastcommit.piilot.global.auth.dto.request.LoginRequestDTO;
import com.lastcommit.piilot.global.auth.dto.request.SignupRequestDTO;
import com.lastcommit.piilot.global.auth.dto.response.SignupResponseDTO;
import com.lastcommit.piilot.global.auth.dto.response.TokenResponseDTO;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.error.status.CommonErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 회원가입
    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request){
        // 비밀번호 확인 검증
        if (!request.password().equals(request.passwordConfirm())) {
            throw new GeneralException(CommonErrorStatus.PASSWORD_NOT_MATCH);
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new GeneralException(CommonErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 암호화 후 저장
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);
        return SignupResponseDTO.from(savedUser);
    }

    // 2. 로그인
    public TokenResponseDTO login(LoginRequestDTO request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new
                        GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new GeneralException(CommonErrorStatus.INVALID_PASSWORD);
        }

        // 토큰 생성 및 반환
        return jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // 3. 토큰 재발급
    public TokenResponseDTO refresh(String refreshToken) {
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new
                    GeneralException(CommonErrorStatus.INVALID_TOKEN);
        }

        // 토큰에서 userId 추출 후 사용자 조회
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new
                        GeneralException(CommonErrorStatus.USER_NOT_FOUND));

        // 새 토큰 생성 및 반환
        return jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
