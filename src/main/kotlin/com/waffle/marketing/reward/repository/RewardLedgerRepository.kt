package com.waffle.marketing.reward.repository

import com.waffle.marketing.reward.model.RewardLedger
import org.springframework.data.jpa.repository.JpaRepository

interface RewardLedgerRepository : JpaRepository<RewardLedger, Long> {
    fun findByUserId(userId: Long): List<RewardLedger>

    /** 미션 완료 여부 확인 (중복 시도 방지용) */
    fun existsByUserIdAndMissionId(
        userId: Long,
        missionId: Long,
    ): Boolean
}
