package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.MissionType

data class MissionDefinitionResponse(
    val id: Long,
    val storeId: Long,
    val type: MissionType,
    val configJson: String,
    val rewardAmount: Int,
    val isActive: Boolean,
    /** 프론트엔드 GPS 검증용 매장 위도/경도 */
    val lat: Double,
    val lng: Double,
)
