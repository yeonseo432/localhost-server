package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.MissionType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import tools.jackson.databind.JsonNode

data class MissionCreateRequest(
    @Schema(description = "미션 유형", example = "TIME_WINDOW")
    @field:NotNull
    val type: MissionType,
    /**
     * 미션별 설정 JSON (JSON 객체 또는 문자열 모두 허용).
     * TIME_WINDOW: {"startHour":15,"endHour":17,"days":["MON","TUE"]}
     * DWELL:       {"durationMinutes":10}
     * RECEIPT:     {"targetProductKey":"아메리카노"}
     * INVENTORY:   {"answerImageUrl":"https://..."}
     * STAMP:       {"requiredCount":5}
     */
    @Schema(description = "미션 설정 JSON", example = "{\"durationMinutes\":10}")
    @field:NotNull
    val configJson: JsonNode,
    @Schema(description = "미션 성공 시 지급 포인트", example = "100")
    @field:NotNull
    @field:Min(0)
    val rewardAmount: Int,
)
