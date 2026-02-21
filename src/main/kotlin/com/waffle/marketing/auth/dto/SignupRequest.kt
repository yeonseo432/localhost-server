package com.waffle.marketing.auth.dto

import com.waffle.marketing.auth.model.UserRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank
    @field:Size(min = 6, max = 12, message = "아이디는 6~12자여야 합니다")
    val username: String,

    @field:NotBlank
    @field:Size(min = 8, max = 12, message = "비밀번호는 8~12자여야 합니다")
    val password: String,

    @field:NotBlank
    val nickname: String,

    val role: UserRole = UserRole.USER,
)
