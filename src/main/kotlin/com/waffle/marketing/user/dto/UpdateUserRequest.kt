package com.waffle.marketing.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @Schema(description = "변경할 아이디 (6~12자)", example = "waffle02")
    @field:Size(min = 6, max = 12, message = "아이디는 6~12자여야 합니다")
    val username: String? = null,
    @Schema(description = "변경할 비밀번호 (8~12자)", example = "newpass12")
    @field:Size(min = 8, max = 12, message = "비밀번호는 8~12자여야 합니다")
    val password: String? = null,
)
