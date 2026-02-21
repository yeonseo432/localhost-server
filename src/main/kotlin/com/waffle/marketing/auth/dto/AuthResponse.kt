package com.waffle.marketing.auth.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val role: String,
)
