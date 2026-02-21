package com.waffle.marketing.mission.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import tools.jackson.databind.JsonNode

data class MissionUpdateRequest(
    @Schema(description = "미션 설정 JSON", example = "{\"startHour\":16,\"endHour\":18}")
    @field:NotNull
    val configJson: JsonNode,
    @Schema(description = "미션 성공 시 지급 포인트", example = "200")
    @field:NotNull
    @field:Min(0)
    val rewardAmount: Int,
    @Schema(description = "활성화 여부 (false=비활성, true=재활성)", example = "true")
    @field:NotNull
    val isActive: Boolean,
)
