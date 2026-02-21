package com.waffle.marketing.auth.service

import com.waffle.marketing.auth.dto.AuthResponse
import com.waffle.marketing.auth.dto.LoginRequest
import com.waffle.marketing.auth.dto.SignupRequest
import com.waffle.marketing.auth.model.User
import com.waffle.marketing.auth.repository.UserRepository
import com.waffle.marketing.common.exception.BadRequestException
import com.waffle.marketing.common.exception.UnauthorizedException
import com.waffle.marketing.config.jwt.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw BadRequestException("이미 사용 중인 아이디입니다: ${request.username}")
        }
        val user = userRepository.save(
            User(
                username = request.username,
                password = request.password,
                nickname = request.nickname,
                role = request.role,
            ),
        )
        val token = jwtTokenProvider.createToken(user.id!!, user.role.name)
        return AuthResponse(token = token, userId = user.id!!, role = user.role.name)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username).orElseThrow {
            UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다")
        }
        if (user.password != request.password) {
            throw UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다")
        }
        val token = jwtTokenProvider.createToken(user.id!!, user.role.name)
        return AuthResponse(token = token, userId = user.id!!, role = user.role.name)
    }
}
