package com.waffle.marketing.store.dto

data class StoreResponse(
    val id: Long,
    val name: String,
    val address: String,
    val detailAddress: String?,
    val lat: Double,
    val lng: Double,
    val ownerId: Long?,
    val businessNumber: String?,
    val imageUrl: String?,
)
