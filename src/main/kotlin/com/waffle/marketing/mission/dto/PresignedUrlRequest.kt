package com.waffle.marketing.mission.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class PresignedUrlRequest(
    @Schema(description = "업로드할 이미지 MIME 타입", example = "image/jpeg")
    @field:NotBlank
    val contentType: String,
)
