package com.waffle.marketing.reward.dto

import java.time.LocalDateTime

data class RewardResponse(
    val id: Long,
    val missionId: Long,
    val amount: Int,
    val createdAt: LocalDateTime,
)
