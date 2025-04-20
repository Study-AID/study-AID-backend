package com.example.api.controller;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입, 로그인, 로그아웃, 토큰 재발급 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/email")
    @Operation(summary = "이메일 회원가입", description = "이메일과 비밀번호, 이름으로 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 가입된 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "message": "이미 가입된 이메일입니다.",
                      "data": null
                    }
                """)
                    )
            )
    })
    public ResponseEntity<CommonResponse<UserSummaryResponse>> signupWithEmail(@RequestBody EmailSignupRequest req) {
        UserSummaryResponse userSummaryResponse = authService.signupWithEmail(req);
        return ResponseEntity.ok(new CommonResponse<>("회원가입 성공", userSummaryResponse));
    }

    @PostMapping("/login/email")
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "존재하지 않는 이메일 or 비밀번호 불일치 or 이메일 로그인 사용자가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "message": "비밀번호가 일치하지 않습니다.",
                      "data": null
                    }
                """)
                    )
            )
    })
    public ResponseEntity<CommonResponse<AuthResponse>> loginWithEmail(@RequestBody EmailLoginRequest req) {
        AuthResponse authResponse = authService.loginWithEmail(req);
        return ResponseEntity.ok(new CommonResponse<>("로그인 성공", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 이용하여 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    public ResponseEntity<CommonResponse<Void>> logout(@RequestBody LogoutRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(new CommonResponse<>("로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 통해 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않거나 저장된 토큰과 불일치, 사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "message": "유효하지 않은 리프레시 토큰입니다.",
                      "data": null
                    }
                """)
                    )
            )
    })
    public ResponseEntity<CommonResponse<AuthResponse>> tokenRefresh(@RequestBody TokenRefreshRequest req) {
        AuthResponse authResponse = authService.tokenRefresh(req);
        return ResponseEntity.ok(new CommonResponse<>("토큰 재발급 성공", authResponse));
    }
}
