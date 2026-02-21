package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.MissionType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MissionCreateRequest(
    @Schema(description = "미션 유형", example = "TIME_WINDOW")
    @field:NotNull
    val type: MissionType,
    /**
     * 미션별 설정 JSON.
     * TIME_WINDOW: {"startHour":15,"endHour":17,"days":["MON","TUE"]}
     * DWELL:       {"durationMinutes":10}
     * RECEIPT:     {"targetProductKey":"아메리카노"}
     * INVENTORY:   {"answerImageUrl":"https://..."}
     * STAMP:       {"requiredCount":5}
     */
    @Schema(description = "미션 설정 JSON", example = "{\"startHour\":15,\"endHour\":17}")
    @field:NotBlank
    val configJson: String,
    @Schema(description = "미션 성공 시 지급 포인트", example = "100")
    @field:NotNull
    @field:Min(0)
    val rewardAmount: Int,
)
