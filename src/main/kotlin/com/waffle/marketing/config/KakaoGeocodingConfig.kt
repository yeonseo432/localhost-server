package com.waffle.marketing.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class KakaoGeocodingConfig {
    @Bean
    fun kakaoRestClient(
        @Value("\${kakao.api-key}") apiKey: String,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK $apiKey")
            .build()
}
