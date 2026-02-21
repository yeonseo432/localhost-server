package com.waffle.marketing.user.service

import com.waffle.marketing.common.exception.ResourceNotFoundException
import com.waffle.marketing.user.dto.UserResponse
import com.waffle.marketing.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getUser(userId: Long): UserResponse {
        val user =
            userRepository.findById(userId).orElseThrow {
                ResourceNotFoundException("사용자를 찾을 수 없습니다: $userId")
            }
        return UserResponse(
            id = user.id!!,
            username = user.username,
            point = user.point,
            role = user.role.name,
        )
    }
}
