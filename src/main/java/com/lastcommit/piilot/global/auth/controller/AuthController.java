package com.lastcommit.piilot.global.auth.controller;

import com.lastcommit.piilot.global.auth.docs.AuthControllerDocs;
import com.lastcommit.piilot.global.auth.dto.request.LoginRequestDTO;
import com.lastcommit.piilot.global.auth.dto.request.SignupRequestDTO;
import com.lastcommit.piilot.global.auth.dto.response.SignupResponseDTO;
import com.lastcommit.piilot.global.auth.dto.response.TokenResponseDTO;
import com.lastcommit.piilot.global.auth.service.AuthService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    public CommonResponse<SignupResponseDTO> signup(
            @Valid @RequestBody SignupRequestDTO request
    ) {
        SignupResponseDTO result = authService.signup(request);
        return CommonResponse.onCreated(result);
    }

    @Override
    @PostMapping("/login")
    public CommonResponse<TokenResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        TokenResponseDTO result = authService.login(request);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/refresh")
    public CommonResponse<TokenResponseDTO> refresh(
            @RequestHeader("Authorization") String refreshToken
    ) {
        String token = refreshToken.replace("Bearer ", "");
        TokenResponseDTO result = authService.refresh(token);
        return CommonResponse.onSuccess(result);
    }
}
