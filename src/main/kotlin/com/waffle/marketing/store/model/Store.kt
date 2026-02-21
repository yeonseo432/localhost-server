package com.waffle.marketing.store.model

import com.waffle.marketing.common.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "stores")
class Store(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var lat: Double,

    @Column(nullable = false)
    var lng: Double,

    @Column(nullable = false)
    var radiusM: Int = 100,

    @Column(length = 255)
    var address: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity()
