package com.waffle.marketing.store.service

import com.waffle.marketing.common.exception.BadRequestException
import com.waffle.marketing.common.exception.ResourceForbiddenException
import com.waffle.marketing.common.extension.ensureNotNull
import com.waffle.marketing.store.dto.StoreRequest
import com.waffle.marketing.store.dto.StoreResponse
import com.waffle.marketing.store.model.Store
import com.waffle.marketing.store.repository.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val kakaoGeocodingClient: KakaoGeocodingClient,
) {
    @Transactional(readOnly = true)
    fun getAll(): List<StoreResponse> = storeRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getMyStores(ownerId: Long): List<StoreResponse> =
        storeRepository.findAllByOwnerId(ownerId).map { it.toResponse() }

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
