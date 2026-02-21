package com.waffle.marketing.mission.model

import com.waffle.marketing.common.model.BaseEntity
import com.waffle.marketing.store.model.Store
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

@Entity
@Table(name = "mission_definitions")
class MissionDefinition(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: MissionType,
    /**
     * 미션별 설정 JSON.
     * TIME_WINDOW: {"startHour":15,"endHour":17,"days":["MON","TUE"]}
     * DWELL:       {"durationMinutes":10}
     * RECEIPT:     {"targetProductKey":"아메리카노","confidenceThreshold":0.8}
     * INVENTORY:   {"answerImageUrl":"https://...", "confidenceThreshold":0.75}
     * STAMP:       {"requiredCount":3}
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    val configJson: String,
    /** 미션 성공 시 지급할 포인트 */
    @Column(nullable = false)
    val rewardAmount: Int,
    @Column(nullable = false)
    var isActive: Boolean = true,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity()
