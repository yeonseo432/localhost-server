package com.waffle.marketing.reward.service

import com.waffle.marketing.mission.model.MissionDefinition
import com.waffle.marketing.reward.dto.RewardResponse
import com.waffle.marketing.reward.model.RewardLedger
import com.waffle.marketing.reward.repository.RewardLedgerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RewardService(
    private val rewardLedgerRepository: RewardLedgerRepository,
) {
    /** 미션 성공 시 리워드 발급. 리워드 ID 반환. */
    @Transactional
    fun issue(
        userId: Long,
        mission: MissionDefinition,
    ): Long {
        val ledger =
            rewardLedgerRepository.save(
                RewardLedger(userId = userId, missionId = mission.id!!, amount = mission.rewardAmount),
            )
        return ledger.id!!
    }

    @Transactional(readOnly = true)
    fun getMyRewards(userId: Long): List<RewardResponse> =
        rewardLedgerRepository.findByUserId(userId).map {
            RewardResponse(
                id = it.id!!,
                missionId = it.missionId,
                amount = it.amount,
                createdAt = it.createdAt,
            )
        }
}
