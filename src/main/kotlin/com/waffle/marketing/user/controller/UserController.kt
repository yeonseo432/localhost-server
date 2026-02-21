package com.waffle.marketing.user.controller

import com.waffle.marketing.user.dto.UserResponse
import com.waffle.marketing.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
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
}
