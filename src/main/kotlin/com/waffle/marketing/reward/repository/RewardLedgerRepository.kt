package com.waffle.marketing.reward.repository

import com.waffle.marketing.reward.model.RewardLedger
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RewardLedgerRepository : JpaRepository<RewardLedger, Long> {
    fun findBySessionId(sessionId: UUID): List<RewardLedger>

    /** 미션 완료 여부 확인 (중복 시도 방지용) */
    fun existsBySessionIdAndMissionId(sessionId: UUID, missionId: Long): Boolean
}
