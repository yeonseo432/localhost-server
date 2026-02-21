package com.waffle.marketing.mission.repository

import com.waffle.marketing.mission.model.AttemptStatus
import com.waffle.marketing.mission.model.MissionAttempt
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MissionAttemptRepository : JpaRepository<MissionAttempt, Long> {
    // M2: 가장 최근 PENDING 체크인을 찾아 checkout에 연결
    fun findTopByUserIdAndMissionIdAndStatusOrderByCreatedAtDesc(
        userId: Long,
        missionId: Long,
        status: AttemptStatus,
    ): MissionAttempt?

    // M5: 오늘 이미 스탬프를 찍었는지 중복 방지
    fun existsByUserIdAndMissionIdAndAttemptDate(
        userId: Long,
        missionId: Long,
        attemptDate: LocalDate,
    ): Boolean

    // M5: 현재까지 쌓인 스탬프 수 (SUCCESS 기준)
    fun countByUserIdAndMissionIdAndStatus(
        userId: Long,
        missionId: Long,
        status: AttemptStatus,
    ): Long

    // 내 미션 이력 조회
    fun findByUserId(userId: Long): List<MissionAttempt>
}
