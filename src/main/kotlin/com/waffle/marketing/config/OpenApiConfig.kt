package com.waffle.marketing.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Waffle Marketing API")
                    .description("미션 리워드 마케팅 서비스 API")
                    .version("v1.0.0"),
            ).components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
            // 전역 보안 적용 — 공개 엔드포인트는 @SecurityRequirements({}) 로 재정의
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
}
