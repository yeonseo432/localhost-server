package com.waffle.marketing.auth.dto

import com.waffle.marketing.auth.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @Schema(description = "아이디 (6~12자)", example = "waffle01")
    @field:NotBlank
    @field:Size(min = 6, max = 12, message = "아이디는 6~12자여야 합니다")
    val username: String,
    @Schema(description = "비밀번호 (8~12자)", example = "pass1234")
    @field:NotBlank
    @field:Size(min = 8, max = 12, message = "비밀번호는 8~12자여야 합니다")
    val password: String,
    @Schema(description = "닉네임", example = "와플유저")
    @field:NotBlank
    val nickname: String,
    @Schema(description = "역할 (USER: 미션 참여 / OWNER: 매장 관리)", example = "USER")
    val role: UserRole = UserRole.USER,
)
