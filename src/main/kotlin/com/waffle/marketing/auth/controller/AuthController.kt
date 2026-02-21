package com.waffle.marketing.auth.controller

import com.waffle.marketing.auth.dto.AuthResponse
import com.waffle.marketing.auth.dto.LoginRequest
import com.waffle.marketing.auth.dto.SignupRequest
import com.waffle.marketing.auth.service.AuthService
import com.waffle.marketing.common.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증")
@SecurityRequirements  // 전역 bearerAuth 적용 제외 (공개 엔드포인트)
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(summary = "회원가입", description = "아이디(6~12자), 비밀번호(8~12자), 닉네임, role(USER|OWNER)을 입력해 계정을 생성합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "가입 성공 — JWT 토큰 반환"),
        ApiResponse(
            responseCode = "400",
            description = "입력값 오류 또는 중복 아이디",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): AuthResponse = authService.signup(request)

    @Operation(summary = "로그인", description = "아이디/비밀번호로 로그인하고 JWT 토큰을 받습니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공 — JWT 토큰 반환"),
        ApiResponse(
            responseCode = "401",
            description = "아이디 또는 비밀번호 불일치",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): AuthResponse = authService.login(request)
}
