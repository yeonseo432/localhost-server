package com.waffle.marketing.session.controller

import com.waffle.marketing.session.dto.SessionResponse
import com.waffle.marketing.session.service.SessionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Session", description = "익명 세션 관리")
@RestController
@RequestMapping("/api/sessions")
class SessionController(
    private val sessionService: SessionService,
) {
    @Operation(summary = "익명 세션 생성", description = "앱 첫 실행 시 익명 세션을 생성하고 JWT 토큰을 반환합니다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSession(): SessionResponse = sessionService.createSession()
}
