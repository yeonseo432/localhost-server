package com.waffle.marketing.mission.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class ImageConfirmRequest(
    @Schema(description = "presigned-url 응답의 imageUrl 값")
    @field:NotBlank
    val imageUrl: String,
)
