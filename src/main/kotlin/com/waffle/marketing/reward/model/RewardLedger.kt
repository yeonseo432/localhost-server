package com.waffle.marketing.reward.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "reward_ledger")
class RewardLedger(
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "mission_id", nullable = false)
    val missionId: Long,
    @Column(nullable = false)
    val amount: Int,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
