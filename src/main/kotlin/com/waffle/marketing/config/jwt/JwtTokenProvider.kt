package com.waffle.marketing.config.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration-in-ms}") private val expirationInMs: Long,
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    fun createToken(
        userId: Long,
        role: String,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expirationInMs))
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long = parseClaims(token).subject.toLong()

    fun getRoleFromToken(token: String): String = parseClaims(token).get("role", String::class.java)

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
