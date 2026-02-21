package com.waffle.marketing.store.service

import com.waffle.marketing.common.exception.BadRequestException
import com.waffle.marketing.common.exception.ResourceForbiddenException
import com.waffle.marketing.common.extension.ensureNotNull
import com.waffle.marketing.store.dto.StoreRequest
import com.waffle.marketing.store.dto.StoreResponse
import com.waffle.marketing.store.model.Store
import com.waffle.marketing.store.repository.StoreRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val kakaoGeocodingClient: KakaoGeocodingClient,
) {
    @Transactional(readOnly = true)
    fun getAll(
        lat: Double?,
        lng: Double?,
    ): List<StoreResponse> {
        if ((lat == null) != (lng == null)) {
            throw BadRequestException("위도와 경도는 둘 다 입력하거나 둘 다 생략해야 합니다")
        }

        val stores = storeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))

        return if (lat != null && lng != null) {
            stores
                .sortedBy { haversineDistance(lat, lng, it.lat, it.lng) }
                .map { it.toResponse() }
        } else {
            stores.map { it.toResponse() }
        }
    }

    @Transactional(readOnly = true)
    fun getMyStores(ownerId: Long): List<StoreResponse> = storeRepository.findAllByOwnerId(ownerId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getById(storeId: Long): StoreResponse =
        storeRepository
            .findById(storeId)
            .orElse(null)
            .ensureNotNull("매장을 찾을 수 없습니다: $storeId")
            .toResponse()

    @Transactional
    fun create(
        request: StoreRequest,
        ownerId: Long,
    ): StoreResponse {
        if (storeRepository.existsByAddressAndDetailAddress(request.address, request.detailAddress)) {
            throw BadRequestException("이미 등록된 주소입니다: ${request.address} ${request.detailAddress ?: ""}".trim())
        }
        val geoPoint = kakaoGeocodingClient.geocode(request.address)
        val store =
            storeRepository.save(
                Store(
                    name = request.name,
                    address = request.address,
                    detailAddress = request.detailAddress,
                    lat = geoPoint.lat,
                    lng = geoPoint.lng,
                    ownerId = ownerId,
                    businessNumber = request.businessNumber,
                    imageUrl = request.imageUrl,
                ),
            )
        return store.toResponse()
    }

    @Transactional
    fun delete(
        storeId: Long,
        ownerId: Long,
    ) {
        val store =
            storeRepository
                .findById(storeId)
                .orElse(null)
                .ensureNotNull("매장을 찾을 수 없습니다: $storeId")

        if (store.ownerId != ownerId) {
            throw ResourceForbiddenException("본인 매장만 삭제할 수 있습니다")
        }

        storeRepository.delete(store)
    }

    private fun haversineDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun Store.toResponse() =
        StoreResponse(
            id = id!!,
            name = name,
            address = address,
            detailAddress = detailAddress,
            lat = lat,
            lng = lng,
            ownerId = ownerId,
            businessNumber = businessNumber,
            imageUrl = imageUrl,
        )
}
