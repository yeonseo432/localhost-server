package com.waffle.marketing.store.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class StoreRequest(
    @field:NotBlank
    val name: String,

    @field:NotNull
    val lat: Double,

    @field:NotNull
    val lng: Double,

    val radiusM: Int = 100,

    val address: String? = null,

    val businessNumber: String? = null,

    val imageUrl: String? = null,
)
