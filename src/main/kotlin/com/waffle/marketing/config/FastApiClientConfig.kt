package com.waffle.marketing.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient

@Configuration
class FastApiClientConfig {
    @Bean
    fun fastApiRestClient(
        @Value("\${fastapi.base-url}") baseUrl: String,
    ): RestClient {
        val httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
        return RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(JdkClientHttpRequestFactory(httpClient))
            .build()
    }
}
