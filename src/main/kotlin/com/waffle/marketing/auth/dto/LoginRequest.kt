package com.waffle.marketing.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LoginRequest(
    @Schema(description = "아이디", example = "waffle01")
    val username: String,

    @Schema(description = "비밀번호", example = "pass1234")
    val password: String,
)
