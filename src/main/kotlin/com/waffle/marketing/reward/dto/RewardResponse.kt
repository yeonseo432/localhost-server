package com.waffle.marketing.reward.dto

import com.waffle.marketing.reward.model.RewardType
import java.time.LocalDateTime

data class RewardResponse(
    val id: Long,
    val missionId: Long,
    val type: RewardType,
    val amountOrCode: String,
    val issuedAt: LocalDateTime,
)
