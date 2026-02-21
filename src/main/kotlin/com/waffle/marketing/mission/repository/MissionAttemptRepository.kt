package com.waffle.marketing.mission.repository

import com.waffle.marketing.mission.model.AttemptStatus
import com.waffle.marketing.mission.model.MissionAttempt
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface MissionAttemptRepository : JpaRepository<MissionAttempt, Long> {

    // M2: 가장 최근 PENDING 체크인을 찾아 checkout에 연결
    fun findTopBySessionIdAndMissionIdAndStatusOrderByCreatedAtDesc(
        sessionId: UUID,
        missionId: Long,
        status: AttemptStatus,
    ): MissionAttempt?

    // M5: 오늘 이미 스탬프를 찍었는지 중복 방지
    fun existsBySessionIdAndMissionIdAndAttemptDate(
        sessionId: UUID,
        missionId: Long,
        attemptDate: LocalDate,
    ): Boolean

    // M5: 현재까지 쌓인 스탬프 수 (SUCCESS 기준)
    fun countBySessionIdAndMissionIdAndStatus(
        sessionId: UUID,
        missionId: Long,
        status: AttemptStatus,
    ): Long

    // 내 미션 이력 조회
    fun findBySessionId(sessionId: UUID): List<MissionAttempt>
}
