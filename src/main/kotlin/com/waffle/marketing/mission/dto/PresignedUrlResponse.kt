package com.waffle.marketing.mission.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PresignedUrlResponse(
    @Schema(description = "S3 presigned PUT URL (10분 유효). 프론트는 이 URL로 직접 PUT 요청해 이미지를 업로드.")
    val presignedUrl: String,
    @Schema(description = "업로드 완료 후 confirm 엔드포인트에 전달할 이미지 URL")
    val imageUrl: String,
)
