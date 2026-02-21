package com.waffle.marketing.store.service

import com.waffle.marketing.store.dto.StoreRequest
import com.waffle.marketing.store.model.Store
import com.waffle.marketing.store.repository.StoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class StoreServiceTest {
    @Mock
    private lateinit var storeRepository: StoreRepository

    @Mock
    private lateinit var kakaoGeocodingClient: KakaoGeocodingClient

    @InjectMocks
    private lateinit var storeService: StoreService

    @Test
    fun `주소로 매장을 등록하면 카카오 API로 변환된 좌표가 저장된다`() {
        // given
        val request =
            StoreRequest(
                name = "와플카페 홍대점",
                address = "서울 마포구 와우산로 21",
                detailAddress = "2층",
            )
        val ownerId = 1L
        val geoPoint = GeoPoint(lat = 37.5563, lng = 126.9236)
        val savedStore =
            Store(
                name = request.name,
                address = request.address,
                detailAddress = request.detailAddress,
                lat = geoPoint.lat,
                lng = geoPoint.lng,
                ownerId = ownerId,
                id = 1L,
            )

        given(kakaoGeocodingClient.geocode(request.address)).willReturn(geoPoint)
        given(storeRepository.save(any(Store::class.java))).willReturn(savedStore)

        // when
        val result = storeService.create(request, ownerId)

        // then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("와플카페 홍대점")
        assertThat(result.address).isEqualTo("서울 마포구 와우산로 21")
        assertThat(result.detailAddress).isEqualTo("2층")
        assertThat(result.lat).isEqualTo(37.5563)
        assertThat(result.lng).isEqualTo(126.9236)
        assertThat(result.ownerId).isEqualTo(ownerId)
    }
}
