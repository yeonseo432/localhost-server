package com.waffle.marketing.user.model

import com.waffle.marketing.common.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 12)
    val username: String,
    @Column(nullable = false, length = 12)
    var password: String,
    @Column(nullable = false)
    var point: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val role: UserRole = UserRole.USER,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity()

enum class UserRole { USER, OWNER }
