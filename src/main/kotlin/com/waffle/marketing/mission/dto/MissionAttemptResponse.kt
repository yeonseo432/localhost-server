package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.AttemptStatus

data class MissionAttemptResponse(
    val attemptId: Long,
    val missionId: Long,
    val status: AttemptStatus,
    /** 재시도 힌트 (M3/M4 실패 시) */
    val retryHint: String? = null,
    /** 성공 시 지급된 리워드 ID */
    val rewardId: Long? = null,
)
