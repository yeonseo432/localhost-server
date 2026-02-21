package com.waffle.marketing.store.dto

data class StoreResponse(
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val radiusM: Int,
    val address: String?,
    val ownerId: Long?,
    val businessNumber: String?,
    val imageUrl: String?,
)
