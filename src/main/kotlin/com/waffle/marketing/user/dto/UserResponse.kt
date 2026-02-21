package com.waffle.marketing.user.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val point: Int,
    val role: String,
)
