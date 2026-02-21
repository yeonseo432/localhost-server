package com.waffle.marketing.mission.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MissionAttemptRequest(
    /**
     * M3(영수증)/M4(재고) 전용: 업로드된 이미지 URL.
     * M1(시간대)/M5(스탬프) 미션은 null 또는 빈 바디({})로 호출.
     * (실제 이미지 업로드는 별도 S3 처리 예정)
     */
    @Schema(description = "M3/M4 이미지 URL (M1·M5는 불필요)", example = "https://example.com/receipt.jpg")
    val imageUrl: String? = null,
)
