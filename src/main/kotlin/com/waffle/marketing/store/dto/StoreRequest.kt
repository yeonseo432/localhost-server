package com.waffle.marketing.store.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class StoreRequest(
    @Schema(description = "매장 이름", example = "와플카페 홍대점")
    @field:NotBlank
    val name: String,
    @Schema(description = "주소", example = "서울 마포구 와우산로 21")
    @field:NotBlank
    val address: String,
    @Schema(description = "상세주소", example = "2층")
    val detailAddress: String? = null,
    @Schema(description = "사업자번호", example = "123-45-67890")
    val businessNumber: String? = null,
    @Schema(description = "매장 대표 이미지 URL", example = "https://example.com/store.jpg")
    val imageUrl: String? = null,
)
