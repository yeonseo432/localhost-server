package com.waffle.marketing.mission.dto

import com.waffle.marketing.mission.model.AttemptStatus
import java.time.LocalDateTime

data class MissionAttemptResponse(
    val attemptId: Long,
    val missionId: Long,
    val status: AttemptStatus,
    /** 재시도 힌트 (M3/M4 미연동 안내, M5 중복 스탬프 안내 등) */
    val retryHint: String? = null,
    /** 성공 시 지급된 리워드 ID */
    val rewardId: Long? = null,
    /** M2 체크인 시각 */
    val checkinAt: LocalDateTime? = null,
    /** M2 체크아웃 시각 */
    val checkoutAt: LocalDateTime? = null,
)
