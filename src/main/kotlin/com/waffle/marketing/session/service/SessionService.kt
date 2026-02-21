package com.waffle.marketing.session.service

import com.waffle.marketing.config.jwt.JwtTokenProvider
import com.waffle.marketing.session.dto.SessionResponse
import com.waffle.marketing.session.model.Session
import com.waffle.marketing.session.repository.SessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional
    fun createSession(): SessionResponse {
        val session = sessionRepository.save(Session())
        val token = jwtTokenProvider.createToken(session.id)
        return SessionResponse(sessionId = session.id, token = token)
    }
}
