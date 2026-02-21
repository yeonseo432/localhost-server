package com.waffle.marketing.mission.service

import com.waffle.marketing.common.exception.MissionAlreadyCompletedException
import com.waffle.marketing.common.extension.ensureNotNull
import com.waffle.marketing.mission.dto.MissionAttemptResponse
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.model.AttemptStatus
import com.waffle.marketing.mission.model.MissionAttempt
import com.waffle.marketing.mission.model.MissionDefinition
import com.waffle.marketing.mission.repository.MissionAttemptRepository
import com.waffle.marketing.mission.repository.MissionDefinitionRepository
import com.waffle.marketing.reward.repository.RewardLedgerRepository
import com.waffle.marketing.reward.service.RewardService
import com.waffle.marketing.session.repository.SessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class MissionService(
    private val missionDefinitionRepository: MissionDefinitionRepository,
    private val missionAttemptRepository: MissionAttemptRepository,
    private val sessionRepository: SessionRepository,
    private val rewardLedgerRepository: RewardLedgerRepository,
    private val rewardService: RewardService,
    private val fastApiMissionClient: FastApiMissionClient,
) {
    @Transactional(readOnly = true)
    fun getMissionsByStore(storeId: Long): List<MissionDefinitionResponse> =
        missionDefinitionRepository.findByStoreIdAndIsActiveTrue(storeId).map { it.toResponse() }

    /** M1: 특정 시간대 방문 */
    @Transactional
    fun attemptTimeWindow(
        sessionId: UUID,
        missionId: Long,
        lat: Double?,
        lng: Double?,
    ): MissionAttemptResponse {
        val (session, mission) = resolveAndGuard(sessionId, missionId)
        // TODO: configJson 파싱 후 현재 시간이 지정 시간대인지 검증
        // TODO: GPS 반경 검증 (옵션)
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(session = session, mission = mission, status = AttemptStatus.SUCCESS),
            )
        val rewardId = rewardService.issue(session, mission)
        return attempt.toResponse(rewardId = rewardId)
    }

    /** M2: 체류 체크인 */
    @Transactional
    fun checkin(
        sessionId: UUID,
        missionId: Long,
        lat: Double,
        lng: Double,
    ): MissionAttemptResponse {
        val (session, mission) = resolveAndGuard(sessionId, missionId)
        // TODO: GPS 반경 검증
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(
                    session = session,
                    mission = mission,
                    status = AttemptStatus.PENDING,
                    checkinAt = LocalDateTime.now(),
                    checkinLat = lat,
                    checkinLng = lng,
                ),
            )
        return attempt.toResponse()
    }

    /** M2: 체류 체크아웃 */
    @Transactional
    fun checkout(
        sessionId: UUID,
        missionId: Long,
        lat: Double,
        lng: Double,
    ): MissionAttemptResponse {
        // checkout은 guard 불필요 (PENDING 상태이므로 아직 완료 전)
        val attempt =
            missionAttemptRepository
                .findTopBySessionIdAndMissionIdAndStatusOrderByCreatedAtDesc(sessionId, missionId, AttemptStatus.PENDING)
                .ensureNotNull("체크인 기록을 찾을 수 없습니다")
        // TODO: configJson 파싱 후 체류 시간 N분 이상인지 검증
        // TODO: GPS 반경 검증
        attempt.checkoutAt = LocalDateTime.now()
        attempt.checkoutLat = lat
        attempt.checkoutLng = lng
        attempt.status = AttemptStatus.SUCCESS
        val rewardId = rewardService.issue(attempt.session, attempt.mission)
        return attempt.toResponse(rewardId = rewardId)
    }

    /** M3: 영수증 이미지 업로드 및 AI 판정 */
    @Transactional
    fun attemptReceipt(
        sessionId: UUID,
        missionId: Long,
        image: MultipartFile,
    ): MissionAttemptResponse {
        val (session, mission) = resolveAndGuard(sessionId, missionId)
        // TODO: S3 업로드 후 imageUrl 획득
        val imageUrl = "TODO_S3_URL"
        val aiResult = fastApiMissionClient.analyzeReceipt(imageUrl, mission.configJson)
        val status = if (aiResult.match) AttemptStatus.SUCCESS else AttemptStatus.RETRY
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(session = session, mission = mission, status = status, imageUrl = imageUrl, aiResultJson = aiResult.rawJson),
            )
        val rewardId = if (status == AttemptStatus.SUCCESS) rewardService.issue(session, mission) else null
        return attempt.toResponse(retryHint = aiResult.retryHint, rewardId = rewardId)
    }

    /** M4: 재고 이미지 비교 판정 */
    @Transactional
    fun attemptInventory(
        sessionId: UUID,
        missionId: Long,
        image: MultipartFile,
    ): MissionAttemptResponse {
        val (session, mission) = resolveAndGuard(sessionId, missionId)
        // TODO: S3 업로드 후 imageUrl 획득
        val imageUrl = "TODO_S3_URL"
        val aiResult = fastApiMissionClient.compareInventory(imageUrl, mission.configJson)
        val status = if (aiResult.match) AttemptStatus.SUCCESS else AttemptStatus.RETRY
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(session = session, mission = mission, status = status, imageUrl = imageUrl, aiResultJson = aiResult.rawJson),
            )
        val rewardId = if (status == AttemptStatus.SUCCESS) rewardService.issue(session, mission) else null
        return attempt.toResponse(retryHint = aiResult.retryHint, rewardId = rewardId)
    }

    /** M5: 반복 방문 스탬프 */
    @Transactional
    fun attemptStamp(
        sessionId: UUID,
        missionId: Long,
        lat: Double?,
        lng: Double?,
    ): MissionAttemptResponse {
        val (session, mission) = resolveAndGuard(sessionId, missionId)

        // 오늘 이미 스탬프를 찍었는지 확인 (하루 1회)
        val alreadyStampedToday =
            missionAttemptRepository
                .existsBySessionIdAndMissionIdAndAttemptDate(sessionId, missionId, LocalDate.now())
        if (alreadyStampedToday) {
            val attempt =
                missionAttemptRepository
                    .findBySessionId(sessionId)
                    .last { it.mission.id == missionId }
            return attempt.toResponse(retryHint = "오늘은 이미 스탬프를 찍었습니다. 내일 다시 방문해 주세요.")
        }

        // TODO: configJson 파싱 후 requiredCount 조회
        val requiredCount = 3L // 임시 하드코딩
        val stampedCount =
            missionAttemptRepository
                .countBySessionIdAndMissionIdAndStatus(sessionId, missionId, AttemptStatus.SUCCESS)

        val isLastStamp = stampedCount + 1 >= requiredCount
        val status = if (isLastStamp) AttemptStatus.SUCCESS else AttemptStatus.PENDING
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(session = session, mission = mission, status = status),
            )
        val rewardId = if (isLastStamp) rewardService.issue(session, mission) else null
        return attempt.toResponse(rewardId = rewardId)
    }

    /** 세션/미션 조회 + 이미 완료된 미션이면 예외 */
    private fun resolveAndGuard(
        sessionId: UUID,
        missionId: Long,
    ): Pair<com.waffle.marketing.session.model.Session, MissionDefinition> {
        if (rewardLedgerRepository.existsBySessionIdAndMissionId(sessionId, missionId)) {
            throw MissionAlreadyCompletedException(missionId)
        }
        return Pair(
            sessionRepository.findById(sessionId).orElse(null).ensureNotNull("세션을 찾을 수 없습니다"),
            missionDefinitionRepository.findById(missionId).orElse(null).ensureNotNull("미션을 찾을 수 없습니다: $missionId"),
        )
    }

    private fun MissionDefinition.toResponse() =
        MissionDefinitionResponse(id = id!!, storeId = store.id!!, type = type, configJson = configJson, rewardJson = rewardJson)

    private fun MissionAttempt.toResponse(
        retryHint: String? = null,
        rewardId: Long? = null,
    ) = MissionAttemptResponse(attemptId = id!!, missionId = mission.id!!, status = status, retryHint = retryHint, rewardId = rewardId)
}
