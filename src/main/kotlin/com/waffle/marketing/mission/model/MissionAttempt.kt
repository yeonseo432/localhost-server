package com.waffle.marketing.mission.model

import com.waffle.marketing.common.model.BaseEntity
import com.waffle.marketing.session.model.Session
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "mission_attempts")
class MissionAttempt(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: Session,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    val mission: MissionDefinition,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: AttemptStatus = AttemptStatus.PENDING,
    // M1/M5: 날짜별 중복 방지용
    @Column(nullable = false)
    val attemptDate: LocalDate = LocalDate.now(),
    // M3/M4: 업로드된 원본 이미지 URL
    @Column(length = 512)
    var imageUrl: String? = null,
    // M2: 체류 체크인
    var checkinAt: LocalDateTime? = null,
    var checkinLat: Double? = null,
    var checkinLng: Double? = null,
    // M2: 체류 체크아웃
    var checkoutAt: LocalDateTime? = null,
    var checkoutLat: Double? = null,
    var checkoutLng: Double? = null,
    // M3/M4: AI 판정 결과 JSON
    // {"confidence":0.92,"extractedItems":["아메리카노"],"retryHint":null}
    @Column(columnDefinition = "TEXT")
    var aiResultJson: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity()
