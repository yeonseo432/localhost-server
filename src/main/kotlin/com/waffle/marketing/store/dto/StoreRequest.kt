package com.waffle.marketing.store.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class StoreRequest(
    @Schema(description = "매장 이름", example = "와플카페 홍대점")
    @field:NotBlank
    val name: String,

    @Schema(description = "위도", example = "37.5563")
    @field:NotNull
    val lat: Double,

    @Schema(description = "경도", example = "126.9236")
    @field:NotNull
    val lng: Double,

    @Schema(description = "반경(m) — 기본 100m", example = "150")
    val radiusM: Int = 100,

    @Schema(description = "주소", example = "서울 마포구 어울마당로 35")
    val address: String? = null,

    @Schema(description = "사업자번호", example = "123-45-67890")
    val businessNumber: String? = null,

    @Schema(description = "매장 대표 이미지 URL", example = "https://example.com/store.jpg")
    val imageUrl: String? = null,
)
