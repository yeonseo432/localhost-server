package com.waffle.marketing.store.service

import com.waffle.marketing.common.exception.BadRequestException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

data class GeoPoint(
    val lat: Double,
    val lng: Double,
)

data class KakaoGeocodingResponse(
    val documents: List<KakaoDocument>,
)

data class KakaoDocument(
    val x: String, // 경도(longitude)
    val y: String, // 위도(latitude)
)

@Component
class KakaoGeocodingClient(
    @Qualifier("kakaoRestClient") private val kakaoRestClient: RestClient,
) {
    fun geocode(address: String): GeoPoint {
        val response =
            kakaoRestClient
                .get()
                .uri("/v2/local/search/address.json?query={query}", address)
                .retrieve()
                .body(KakaoGeocodingResponse::class.java)
                ?: throw BadRequestException("카카오 주소 검색에 실패했습니다")

        val document =
            response.documents.firstOrNull()
                ?: throw BadRequestException("주소를 찾을 수 없습니다: $address")

        return GeoPoint(
            lat = document.y.toDouble(),
            lng = document.x.toDouble(),
        )
    }
}
