package com.waffle.marketing.store.service

import com.waffle.marketing.common.exception.BadRequestException
import com.waffle.marketing.common.exception.ExternalApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestClientException

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
    private val log = LoggerFactory.getLogger(javaClass)

    fun geocode(address: String): GeoPoint {
        log.info("카카오 주소 검색 요청: address={}", address)

        val response =
            try {
                kakaoRestClient
                    .get()
                    .uri("/v2/local/search/address.json?query={query}", address)
                    .retrieve()
                    .body(KakaoGeocodingResponse::class.java)
            } catch (e: RestClientResponseException) {
                log.error("카카오 API HTTP 오류: status={}, body={}", e.statusCode, e.responseBodyAsString)
                throw ExternalApiException("카카오 API 호출 실패 (HTTP ${e.statusCode.value()}): ${e.responseBodyAsString}")
            } catch (e: RestClientException) {
                log.error("카카오 API 네트워크 오류: {}", e.message)
                throw ExternalApiException("카카오 API 네트워크 오류: ${e.message}")
            }

        if (response == null) {
            log.error("카카오 API 응답이 null: address={}", address)
            throw ExternalApiException("카카오 API 응답을 파싱할 수 없습니다")
        }

        log.info("카카오 주소 검색 결과: address={}, count={}", address, response.documents.size)

        val document =
            response.documents.firstOrNull()
                ?: run {
                    log.warn("주소 검색 결과 없음: address={}", address)
                    throw BadRequestException("주소를 찾을 수 없습니다: $address")
                }

        return try {
            GeoPoint(
                lat = document.y.toDouble(),
                lng = document.x.toDouble(),
            )
        } catch (e: NumberFormatException) {
            log.error("좌표 변환 실패: x={}, y={}", document.x, document.y)
            throw ExternalApiException("카카오 API 좌표 형식 오류: x=${document.x}, y=${document.y}")
        }
    }
}
