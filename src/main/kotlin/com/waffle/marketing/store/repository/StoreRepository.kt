package com.waffle.marketing.store.repository

import com.waffle.marketing.store.model.Store
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<Store, Long> {
    fun existsByAddressAndDetailAddress(
        address: String,
        detailAddress: String?,
    ): Boolean

    fun existsByOwnerId(ownerId: Long): Boolean

    fun findAllByOwnerId(ownerId: Long): List<Store>
}
