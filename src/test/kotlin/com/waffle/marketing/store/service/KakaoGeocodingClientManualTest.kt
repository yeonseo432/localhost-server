package com.waffle.marketing.store.service

import com.waffle.marketing.config.KakaoGeocodingConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 카카오 Geocoding API 실제 호출 수동 테스트.
 * application-local.yaml 의 kakao.api-key 를 사용하므로 별도 환경변수 불필요.
 * CI에서는 실행되지 않음 (@Disabled).
 */
@Disabled("수동 테스트 - 실행 시 @Disabled 제거")
@SpringBootTest(classes = [KakaoGeocodingConfig::class, KakaoGeocodingClient::class])
@ActiveProfiles("local")
class KakaoGeocodingClientManualTest {
    @Autowired
    private lateinit var client: KakaoGeocodingClient

    @Test
    fun `실제 주소로 카카오 API를 호출하면 서울 좌표가 반환된다`() {
        val geoPoint = client.geocode("서울 마포구 와우산로 21")

        println("결과 → lat=${geoPoint.lat}, lng=${geoPoint.lng}")

        assertThat(geoPoint.lat).isBetween(37.0, 38.0)
        assertThat(geoPoint.lng).isBetween(126.0, 127.5)
    }
}
