package com.waffle.marketing.config.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration-in-ms}") private val expirationInMs: Long,
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    fun createToken(sessionId: UUID): String {
        val now = Date()
        return Jwts.builder()
            .subject(sessionId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expirationInMs))
            .signWith(key)
            .compact()
    }

    fun getSessionIdFromToken(token: String): UUID = UUID.fromString(parseClaims(token).subject)

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
