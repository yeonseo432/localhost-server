package com.waffle.marketing.reward.model

import com.waffle.marketing.mission.model.MissionDefinition
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
import java.time.LocalDateTime

@Entity
@Table(name = "reward_ledger")
class RewardLedger(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: Session,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    val mission: MissionDefinition,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val type: RewardType,

    /**
     * POINT인 경우 숫자 문자열 ("100"),
     * COUPON인 경우 쿠폰코드 ("WELCOME2024")
     */
    @Column(nullable = false, length = 100)
    val amountOrCode: String,

    @Column(nullable = false)
    val issuedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
