package com.waffle.marketing.session.dto

import java.util.UUID

data class SessionResponse(
    val sessionId: UUID,
    val token: String,
)
