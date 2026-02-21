package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.MissionType

data class MissionDefinitionResponse(
    val id: Long,
    val storeId: Long,
    val type: MissionType,
    val configJson: String,
    val rewardAmount: Int,
)
