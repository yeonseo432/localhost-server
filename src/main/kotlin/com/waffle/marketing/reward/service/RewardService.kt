package com.waffle.marketing.reward.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.waffle.marketing.mission.model.MissionDefinition
import com.waffle.marketing.reward.dto.RewardResponse
import com.waffle.marketing.reward.model.RewardLedger
import com.waffle.marketing.reward.model.RewardType
import com.waffle.marketing.reward.repository.RewardLedgerRepository
import com.waffle.marketing.session.model.Session
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RewardService(
    private val rewardLedgerRepository: RewardLedgerRepository,
    private val objectMapper: ObjectMapper,
) {
    /** 미션 성공 시 리워드 발급. 리워드 ID 반환. */
    @Transactional
    fun issue(session: Session, mission: MissionDefinition): Long {
        val rewardConfig = objectMapper.readTree(mission.rewardJson)
        val type = RewardType.valueOf(rewardConfig["type"].asText())
        val amountOrCode = when (type) {
            RewardType.POINT -> rewardConfig["amount"].asText()
            RewardType.COUPON -> rewardConfig["code"].asText()
        }
        val ledger = rewardLedgerRepository.save(
            RewardLedger(session = session, mission = mission, type = type, amountOrCode = amountOrCode),
        )
        return ledger.id!!
    }

    @Transactional(readOnly = true)
    fun getMyRewards(sessionId: UUID): List<RewardResponse> =
        rewardLedgerRepository.findBySessionId(sessionId).map {
            RewardResponse(
                id = it.id!!,
                missionId = it.mission.id!!,
                type = it.type,
                amountOrCode = it.amountOrCode,
                issuedAt = it.issuedAt,
            )
        }
}
