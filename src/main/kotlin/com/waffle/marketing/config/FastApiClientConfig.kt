package com.waffle.marketing.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class FastApiClientConfig {
    @Bean
    fun fastApiRestClient(
        @Value("\${fastapi.base-url}") baseUrl: String,
    ): RestClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .build()
}
