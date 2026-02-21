package com.waffle.marketing.mission.service

data class AiJudgmentResult(
    val match: Boolean,
    val confidence: Double,
    val retryHint: String?,
    val rawJson: String,
)
