package com.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.service.AuthService;
import com.example.api.entity.User;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입, 로그인, 로그아웃, 토큰 재발급 관련 API")

public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/email")
    @Operation(summary = "이메일 회원가입", description = "이메일과 비밀번호, 이름으로 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "이메일 회원가입 성공",
                                  "data": {
                                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "email": "test@example.com",
                                    "name": "테스트"
                                  }
                                }
                            """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "이미 가입된 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "이미 가입된 이메일입니다.",
                                  "data": null
                                }
                            """)
                    )
            )
    })
    public ResponseEntity<SimpleResponse<UserSummaryResponse>> signupWithEmail(@RequestBody EmailSignupRequest req) {
        UserSummaryResponse userSummaryResponse = authService.signupWithEmail(req);
        return ResponseEntity.ok(new SimpleResponse<>("이메일 회원가입 성공", userSummaryResponse));
    }

    @PostMapping("/login/email")
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "message": "이메일 로그인 성공",
                              "data": {
                                "token": {
                                  "accessToken": "sample-access-token",
                                  "refreshToken": "sample-refresh-token"
                                },
                                "user": {
                                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                  "email": "test@example.com",
                                  "name": "테스트"
                                }
                              }
                            }
                        """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "이메일/비밀번호 입력 오류 또는 로그인 방식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "wrong-login-input", value = """
                                    {
                                      "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
                                      "data": null
                                    }
                                """),
                                    @ExampleObject(name = "wrong-auth-type", value = """
                                    {
                                      "message": "이메일 로그인 사용자가 아닙니다. 다른 로그인 방식으로 시도해보세요.",
                                      "data": null
                                    }
                                """)
                            }
                    ))
    })
    public ResponseEntity<SimpleResponse<AuthResponse>> loginWithEmail(@RequestBody EmailLoginRequest req) {
        AuthResponse authResponse = authService.loginWithEmail(req);
        return ResponseEntity.ok(new SimpleResponse<>("이메일 로그인 성공", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 이용하여 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "로그아웃 성공",
                                  "data": null
                                }
                            """)
                    )
            )
    })
    public ResponseEntity<SimpleResponse<Void>> logout(@RequestBody LogoutRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(new SimpleResponse<>("로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 통해 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "토큰 재발급 성공",
                                  "data": {
                                    "token": {
                                      "accessToken": "new-access-token",
                                      "refreshToken": "new-refresh-token"
                                    },
                                    "user": {
                                      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "email": "test@example.com",
                                      "name": "테스트"
                                    }
                                  }
                                }
                            """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유효하지 않은 토큰", value = """
                                                {
                                                  "message": "유효하지 않은 리프레시 토큰입니다.",
                                                  "data": null
                                                }
                                            """),
                                    @ExampleObject(name = "저장된 값과 불일치", value = """
                                                {
                                                  "message": "리프레시 토큰이 일치하지 않습니다.",
                                                  "data": null
                                                }
                                            """)
                            }
                    )),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "사용자를 찾을 수 없습니다.",
                                  "data": null
                                }
                            """)
                    ))
    })
    public ResponseEntity<SimpleResponse<AuthResponse>> refreshToken(@RequestBody TokenRefreshRequest req) {
        AuthResponse authResponse = authService.refreshToken(req);
        return ResponseEntity.ok(new SimpleResponse<>("토큰 재발급 성공", authResponse));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회 (Access Token 유효성 확인용)", description = "로그인한 사용자의 정보를 조회하거나, 액세스 토큰 유효성을 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공, 액세스 토큰 유효함",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "message": "사용자 정보 조회 성공",
                              "data": {
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                "email": "test@example.com",
                                "name": "테스트"
                              }
                            }
                        """)
                    )),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "message": "유효하지 않은 액세스 토큰입니다.",
                              "data": null
                            }
                        """)
                    )),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "message": "사용자를 찾을 수 없습니다.",
                              "data": null
                            }
                        """)
                    ))
    })
    public ResponseEntity<SimpleResponse<UserSummaryResponse>> getCurrentUserInfo(Authentication authentication) {
        String userId = authentication.getName();
        UserSummaryResponse userSummaryResponse = authService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(new SimpleResponse<>("사용자 정보 조회 성공", userSummaryResponse));
    }
}
