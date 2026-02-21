package com.waffle.marketing.mission.service

import com.waffle.marketing.common.exception.BadRequestException
import com.waffle.marketing.common.exception.MissionAlreadyCompletedException
import com.waffle.marketing.common.exception.ResourceForbiddenException
import com.waffle.marketing.common.extension.ensureNotNull
import com.waffle.marketing.mission.dto.MissionAttemptRequest
import com.waffle.marketing.mission.dto.MissionAttemptResponse
import com.waffle.marketing.mission.dto.MissionCreateRequest
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.dto.MissionUpdateRequest
import com.waffle.marketing.mission.dto.PresignedUrlResponse
import com.waffle.marketing.mission.model.AttemptStatus
import com.waffle.marketing.mission.model.MissionAttempt
import com.waffle.marketing.mission.model.MissionDefinition
import com.waffle.marketing.mission.model.MissionType
import com.waffle.marketing.mission.repository.MissionAttemptRepository
import com.waffle.marketing.mission.repository.MissionDefinitionRepository
import com.waffle.marketing.reward.repository.RewardLedgerRepository
import com.waffle.marketing.reward.service.RewardService
import com.waffle.marketing.store.repository.StoreRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class MissionService(
    private val missionDefinitionRepository: MissionDefinitionRepository,
    private val missionAttemptRepository: MissionAttemptRepository,
    private val rewardLedgerRepository: RewardLedgerRepository,
    private val rewardService: RewardService,
    private val storeRepository: StoreRepository,
    private val s3ImageServiceProvider: ObjectProvider<S3ImageService>,
    private val objectMapper: ObjectMapper,
    private val fastApiMissionClient: FastApiMissionClient,
) {
    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** 매장 미션 목록 조회 (활성 미션만) */
    @Transactional(readOnly = true)
    fun getMissionsByStore(storeId: Long): List<MissionDefinitionResponse> =
        missionDefinitionRepository.findByStoreIdAndIsActiveTrue(storeId).map { it.toResponse() }

    /** 미션 단건 조회 */
    @Transactional(readOnly = true)
    fun getMissionById(
        storeId: Long,
        missionId: Long,
    ): MissionDefinitionResponse {
        val mission =
            missionDefinitionRepository
                .findById(missionId)
                .orElse(null)
                .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        if (mission.store.id != storeId) throw BadRequestException("해당 매장의 미션이 아닙니다")
        return mission.toResponse()
    }

    /** 미션 등록 (OWNER 전용, 본인 매장만) */
    @Transactional
    fun createMission(
        storeId: Long,
        request: MissionCreateRequest,
        ownerId: Long,
    ): MissionDefinitionResponse {
        val store =
            storeRepository
                .findById(storeId)
                .orElse(null)
                .ensureNotNull("매장을 찾을 수 없습니다: $storeId")
        if (store.ownerId != ownerId) throw ResourceForbiddenException("해당 매장의 소유자가 아닙니다")
        validateConfigJson(request.type, request.configJson)
        val mission =
            missionDefinitionRepository.save(
                MissionDefinition(
                    store = store,
                    type = request.type,
                    configJson = request.configJson,
                    rewardAmount = request.rewardAmount,
                ),
            )
        return mission.toResponse()
    }

    /** 미션 수정 (OWNER 전용, 본인 매장만). type은 변경 불가. */
    @Transactional
    fun updateMission(
        storeId: Long,
        missionId: Long,
        request: MissionUpdateRequest,
        ownerId: Long,
    ): MissionDefinitionResponse {
        val mission =
            missionDefinitionRepository
                .findById(missionId)
                .orElse(null)
                .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        if (mission.store.id != storeId) throw BadRequestException("해당 매장의 미션이 아닙니다")
        if (mission.store.ownerId != ownerId) throw ResourceForbiddenException("해당 매장의 소유자가 아닙니다")
        validateConfigJson(mission.type, request.configJson)
        mission.configJson = request.configJson
        mission.rewardAmount = request.rewardAmount
        mission.isActive = request.isActive
        return mission.toResponse()
    }

    /** 미션 삭제 (소프트 삭제 — isActive=false, OWNER 전용, 본인 매장만) */
    @Transactional
    fun deleteMission(
        storeId: Long,
        missionId: Long,
        ownerId: Long,
    ) {
        val mission =
            missionDefinitionRepository
                .findById(missionId)
                .orElse(null)
                .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        if (mission.store.id != storeId) throw BadRequestException("해당 매장의 미션이 아닙니다")
        if (mission.store.ownerId != ownerId) throw ResourceForbiddenException("해당 매장의 소유자가 아닙니다")
        mission.isActive = false
    }

    // ── 이미지 업로드 (INVENTORY 답안 이미지) ────────────────────────────────

    /** INVENTORY 답안 이미지 업로드용 presigned PUT URL 발급 — 미션 생성 전 (missionId 불필요, OWNER 전용, prod 전용) */
    fun generateStoreInventoryPresignedUrl(
        storeId: Long,
        ownerId: Long,
        contentType: String,
    ): PresignedUrlResponse {
        val store =
            storeRepository
                .findById(storeId)
                .orElse(null)
                .ensureNotNull("매장을 찾을 수 없습니다: $storeId")
        if (store.ownerId != ownerId) throw ResourceForbiddenException("해당 매장의 소유자가 아닙니다")
        val s3 = s3ImageServiceProvider.ifAvailable ?: throw BadRequestException("S3가 구성되지 않은 환경입니다")
        val (presignedUrl, imageUrl) = s3.generateStorePresignedPutUrl(storeId, contentType)
        return PresignedUrlResponse(presignedUrl = presignedUrl, imageUrl = imageUrl)
    }

    /** INVENTORY 미션 답안 이미지 업로드용 presigned PUT URL 발급 — 기존 미션 수정용 (OWNER 전용, prod 전용) */
    fun generateAnswerImagePresignedUrl(
        storeId: Long,
        missionId: Long,
        ownerId: Long,
        contentType: String,
    ): PresignedUrlResponse {
        val mission = resolveOwnerMission(storeId, missionId, ownerId)
        if (mission.type != MissionType.INVENTORY) throw BadRequestException("INVENTORY 미션만 이미지를 업로드할 수 있습니다")
        val s3 = s3ImageServiceProvider.ifAvailable ?: throw BadRequestException("S3가 구성되지 않은 환경입니다")
        val (presignedUrl, imageUrl) = s3.generatePresignedPutUrl(missionId, contentType)
        return PresignedUrlResponse(presignedUrl = presignedUrl, imageUrl = imageUrl)
    }

    /**
     * 프론트가 S3 업로드 완료 후 호출.
     * 기존 answerImageUrl이 우리 버킷 소속이면 S3에서 삭제하고, configJson을 신규 URL로 갱신.
     */
    @Transactional
    fun confirmAnswerImageUpload(
        storeId: Long,
        missionId: Long,
        ownerId: Long,
        imageUrl: String,
    ): MissionDefinitionResponse {
        val mission = resolveOwnerMission(storeId, missionId, ownerId)
        if (mission.type != MissionType.INVENTORY) throw BadRequestException("INVENTORY 미션만 이미지를 업로드할 수 있습니다")

        val configNode = objectMapper.readTree(mission.configJson) as ObjectNode
        val oldImageUrl = configNode.get("answerImageUrl")?.textValue()
        if (!oldImageUrl.isNullOrBlank()) {
            s3ImageServiceProvider.ifAvailable?.deleteIfOurs(oldImageUrl)
        }
        configNode.put("answerImageUrl", imageUrl)
        mission.configJson = objectMapper.writeValueAsString(configNode)
        return mission.toResponse()
    }

    // ── 판독 (Attempt) ────────────────────────────────────────────────────────

    /**
     * 미션 판독 (단일 엔드포인트).
     * - M1(시간대): 빈 바디 또는 {}
     * - M2(체류): 사용 불가 — /attempts/checkin, /attempts/checkout 엔드포인트 사용
     * - M3(영수증)/M4(재고): request.imageUrl 필수
     * - M5(스탬프): 빈 바디 또는 {}
     */
    @Transactional
    fun attemptMission(
        userId: Long,
        missionId: Long,
        request: MissionAttemptRequest,
    ): MissionAttemptResponse {
        val mission = resolveAndGuard(userId, missionId)
        return when (mission.type) {
            MissionType.TIME_WINDOW -> handleTimeWindow(userId, missionId, mission)
            MissionType.DWELL -> throw BadRequestException("M2 체류 미션은 /attempts/checkin 및 /attempts/checkout 엔드포인트를 사용하세요")
            MissionType.RECEIPT -> handleReceipt(userId, missionId, mission, request.imageUrl)
            MissionType.INVENTORY -> handleInventory(userId, missionId, mission, request.imageUrl)
            MissionType.STAMP -> handleStamp(userId, missionId, mission)
        }
    }

    /** 내 미션 수행 이력 조회 */
    @Transactional(readOnly = true)
    fun getMyAttempts(
        userId: Long,
        missionId: Long,
    ): List<MissionAttemptResponse> =
        missionAttemptRepository
            .findByUserIdAndMissionId(userId, missionId)
            .map { it.toResponse() }

    /** M2: 체류 체크인 */
    @Transactional
    fun checkin(
        userId: Long,
        missionId: Long,
    ): MissionAttemptResponse {
        missionDefinitionRepository
            .findById(missionId)
            .orElse(null)
            .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        // TODO: GPS 반경 검증 (프론트엔드에서 처리됨 — 서버는 store lat/lng만 제공)
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(
                    userId = userId,
                    missionId = missionId,
                    status = AttemptStatus.PENDING,
                    checkinAt = LocalDateTime.now(),
                ),
            )
        return attempt.toResponse()
    }

    /** M2: 체류 체크아웃 */
    @Transactional
    fun checkout(
        userId: Long,
        missionId: Long,
    ): MissionAttemptResponse {
        val attempt =
            missionAttemptRepository
                .findTopByUserIdAndMissionIdAndStatusOrderByCreatedAtDesc(userId, missionId, AttemptStatus.PENDING)
                .ensureNotNull("체크인 기록을 찾을 수 없습니다")
        val mission =
            missionDefinitionRepository
                .findById(missionId)
                .orElse(null)
                .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        val durationMinutes = objectMapper.readTree(mission.configJson).get("durationMinutes").asLong()
        val checkinTime = attempt.checkinAt ?: throw BadRequestException("체크인 시간을 찾을 수 없습니다")
        val now = LocalDateTime.now()
        val elapsedMinutes = ChronoUnit.MINUTES.between(checkinTime, now)

        attempt.checkoutAt = now
        return if (elapsedMinutes >= durationMinutes) {
            attempt.status = AttemptStatus.SUCCESS
            val rewardId = rewardService.issue(userId, mission)
            attempt.toResponse(rewardId = rewardId)
        } else {
            attempt.status = AttemptStatus.FAILED
            val remaining = durationMinutes - elapsedMinutes
            attempt.toResponse(retryHint = "체류 시간 부족 (필요: ${durationMinutes}분, 실제: ${elapsedMinutes}분, 남은 시간: ${remaining}분)")
        }
    }

    // ── 미션 유형별 내부 처리 ─────────────────────────────────────────────────

    /** M1: 특정 시간대 방문 — configJson: {"startHour":15,"endHour":17,"days":["MON","TUE"]} */
    private fun handleTimeWindow(
        userId: Long,
        missionId: Long,
        mission: MissionDefinition,
    ): MissionAttemptResponse {
        val node = objectMapper.readTree(mission.configJson)
        val startHour = node.get("startHour").asInt()
        val endHour = node.get("endHour").asInt()
        val days = node.get("days")?.map { it.textValue() ?: "" } ?: emptyList()

        val now = LocalDateTime.now()
        // DayOfWeek.name → "MONDAY" → take(3) → "MON"
        val currentDay = now.dayOfWeek.name.take(3)
        val currentHour = now.hour
        // startHour 이상 endHour 미만 (예: 15~17 → 15:00~16:59)
        val inTimeRange = currentHour in startHour until endHour
        val inDay = days.isEmpty() || days.contains(currentDay)

        val status = if (inTimeRange && inDay) AttemptStatus.SUCCESS else AttemptStatus.FAILED
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(userId = userId, missionId = missionId, status = status),
            )
        val rewardId = if (status == AttemptStatus.SUCCESS) rewardService.issue(userId, mission) else null
        val retryHint =
            if (status == AttemptStatus.FAILED) {
                "미션 가능 시간: ${startHour}시~${endHour}시 / 가능 요일: ${days.joinToString(", ")}"
            } else {
                null
            }
        return attempt.toResponse(retryHint = retryHint, rewardId = rewardId)
    }

    /** M3: 영수증 이미지 AI 판독 */
    private fun handleReceipt(
        userId: Long,
        missionId: Long,
        mission: MissionDefinition,
        imageUrl: String?,
    ): MissionAttemptResponse {
        val url = imageUrl ?: throw BadRequestException("M3 영수증 미션은 imageUrl이 필요합니다")
        val aiResult = fastApiMissionClient.analyzeReceipt(url, mission.configJson)
        val status = if (aiResult.match) AttemptStatus.SUCCESS else AttemptStatus.RETRY
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(
                    userId = userId,
                    missionId = missionId,
                    status = status,
                    imageUrl = url,
                    aiResultJson = aiResult.rawJson,
                ),
            )
        val rewardId = if (status == AttemptStatus.SUCCESS) rewardService.issue(userId, mission) else null
        return attempt.toResponse(retryHint = aiResult.retryHint, rewardId = rewardId)
    }

    /** M4: 재고 이미지 AI 비교 판독 */
    private fun handleInventory(
        userId: Long,
        missionId: Long,
        mission: MissionDefinition,
        imageUrl: String?,
    ): MissionAttemptResponse {
        val url = imageUrl ?: throw BadRequestException("M4 재고 미션은 imageUrl이 필요합니다")
        val aiResult = fastApiMissionClient.compareInventory(url, mission.configJson)
        val status = if (aiResult.match) AttemptStatus.SUCCESS else AttemptStatus.RETRY
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(
                    userId = userId,
                    missionId = missionId,
                    status = status,
                    imageUrl = url,
                    aiResultJson = aiResult.rawJson,
                ),
            )
        val rewardId = if (status == AttemptStatus.SUCCESS) rewardService.issue(userId, mission) else null
        return attempt.toResponse(retryHint = aiResult.retryHint, rewardId = rewardId)
    }

    /** M5: 반복 방문 스탬프 */
    private fun handleStamp(
        userId: Long,
        missionId: Long,
        mission: MissionDefinition,
    ): MissionAttemptResponse {
        val alreadyStampedToday =
            missionAttemptRepository
                .existsByUserIdAndMissionIdAndAttemptDate(userId, missionId, LocalDate.now())
        if (alreadyStampedToday) {
            val todayAttempt =
                missionAttemptRepository
                    .findTopByUserIdAndMissionIdAndAttemptDateOrderByIdDesc(userId, missionId, LocalDate.now())
                    .ensureNotNull("오늘 스탬프 기록을 찾을 수 없습니다")
            return todayAttempt.toResponse(retryHint = "오늘은 이미 스탬프를 찍었습니다. 내일 다시 방문해 주세요.")
        }
        val requiredCount = objectMapper.readTree(mission.configJson).get("requiredCount").asLong()
        val stampedCount =
            missionAttemptRepository
                .countByUserIdAndMissionIdAndStatus(userId, missionId, AttemptStatus.SUCCESS)
        val isLastStamp = stampedCount + 1 >= requiredCount
        val status = if (isLastStamp) AttemptStatus.SUCCESS else AttemptStatus.PENDING
        val attempt =
            missionAttemptRepository.save(
                MissionAttempt(userId = userId, missionId = missionId, status = status),
            )
        val rewardId = if (isLastStamp) rewardService.issue(userId, mission) else null
        return attempt.toResponse(rewardId = rewardId)
    }

    // ── 공통 헬퍼 ────────────────────────────────────────────────────────────

    /**
     * 미션 유형별 configJson 필수 필드 검증.
     * 등록(POST)·수정(PUT) 시 공통으로 호출.
     */
    private fun validateConfigJson(
        type: MissionType,
        configJson: String,
    ) {
        val node =
            runCatching { objectMapper.readTree(configJson) }
                .getOrElse { throw BadRequestException("configJson이 유효한 JSON이 아닙니다") }

        fun requireInt(
            field: String,
            min: Int = Int.MIN_VALUE,
            max: Int = Int.MAX_VALUE,
        ) {
            val v = node.get(field)?.asInt() ?: throw BadRequestException("configJson에 '$field' 필드가 필요합니다")
            if (v < min || v > max) throw BadRequestException("'$field'는 $min~$max 범위여야 합니다 (현재: $v)")
        }

        fun requireString(field: String) {
            val v = node.get(field)?.textValue() ?: throw BadRequestException("configJson에 '$field' 필드가 필요합니다")
            if (v.isBlank()) throw BadRequestException("'$field'는 빈 값일 수 없습니다")
        }

        when (type) {
            MissionType.TIME_WINDOW -> {
                // {"startHour":15,"endHour":17,"days":["MON","TUE"]}
                requireInt("startHour", min = 0, max = 23)
                requireInt("endHour", min = 1, max = 24)
                val start = node.get("startHour").asInt()
                val end = node.get("endHour").asInt()
                if (start >= end) throw BadRequestException("'startHour'($start)는 'endHour'($end)보다 작아야 합니다")
                val days = node.get("days") ?: throw BadRequestException("configJson에 'days' 필드가 필요합니다")
                if (!days.isArray || days.size() == 0) throw BadRequestException("'days'는 비어 있지 않은 배열이어야 합니다 (예: [\"MON\",\"TUE\"])")
            }
            MissionType.DWELL -> {
                // {"durationMinutes":10}
                requireInt("durationMinutes", min = 1)
            }
            MissionType.RECEIPT -> {
                // {"targetProductKey":"아메리카노"}
                requireString("targetProductKey")
            }
            MissionType.INVENTORY -> {
                // {"answerImageUrl":"https://..."} — 프론트에서 미리 S3에 업로드 후 URL을 설정
                requireString("answerImageUrl")
            }
            MissionType.STAMP -> {
                // {"requiredCount":5}
                requireInt("requiredCount", min = 1)
            }
        }
    }

    /** 미션 조회 + 매장·소유자 검증 (OWNER 전용 작업에 사용) */
    private fun resolveOwnerMission(
        storeId: Long,
        missionId: Long,
        ownerId: Long,
    ): MissionDefinition {
        val mission =
            missionDefinitionRepository
                .findById(missionId)
                .orElse(null)
                .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
        if (mission.store.id != storeId) throw BadRequestException("해당 매장의 미션이 아닙니다")
        if (mission.store.ownerId != ownerId) throw ResourceForbiddenException("해당 매장의 소유자가 아닙니다")
        return mission
    }

    /** 미션 조회 + 이미 완료된 미션이면 예외 */
    private fun resolveAndGuard(
        userId: Long,
        missionId: Long,
    ): MissionDefinition {
        if (rewardLedgerRepository.existsByUserIdAndMissionId(userId, missionId)) {
            throw MissionAlreadyCompletedException(missionId)
        }
        return missionDefinitionRepository
            .findById(missionId)
            .orElse(null)
            .ensureNotNull("미션을 찾을 수 없습니다: $missionId")
    }

    private fun MissionDefinition.toResponse() =
        MissionDefinitionResponse(
            id = id!!,
            storeId = store.id!!,
            type = type,
            configJson = configJson,
            rewardAmount = rewardAmount,
            isActive = isActive,
            lat = store.lat,
            lng = store.lng,
        )

    private fun MissionAttempt.toResponse(
        retryHint: String? = null,
        rewardId: Long? = null,
    ) = MissionAttemptResponse(
        attemptId = id!!,
        missionId = missionId,
        status = status,
        retryHint = retryHint,
        rewardId = rewardId,
        checkinAt = checkinAt,
        checkoutAt = checkoutAt,
    )
}
