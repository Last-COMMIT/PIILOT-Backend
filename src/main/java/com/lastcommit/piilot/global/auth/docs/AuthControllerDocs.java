package com.lastcommit.piilot.global.auth.docs;

import com.lastcommit.piilot.global.auth.dto.request.LoginRequestDTO;
import com.lastcommit.piilot.global.auth.dto.request.SignupRequestDTO;
import com.lastcommit.piilot.global.auth.dto.response.SignupResponseDTO;
import com.lastcommit.piilot.global.auth.dto.response.TokenResponseDTO;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    CommonResponse<SignupResponseDTO> signup(SignupRequestDTO request);

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "비밀번호 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    CommonResponse<TokenResponseDTO> login(LoginRequestDTO request);

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    CommonResponse<TokenResponseDTO> refresh(String refreshToken);
}
