package com.waffle.marketing.user.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.user.dto.UserResponse
import com.waffle.marketing.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "내 정보 조회", description = "로그인한 유저의 프로필(포인트 포함)을 반환합니다.")
    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal userId: Long,
    ): UserResponse = userService.getUser(userId)

    @Operation(summary = "회원 탈퇴", description = "본인 계정을 삭제합니다. OWNER는 매장을 먼저 삭제해야 합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        ApiResponse(
            responseCode = "400",
            description = "등록된 매장 있음 (OWNER)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMe(
        @AuthenticationPrincipal userId: Long,
    ) = userService.delete(userId)
}
