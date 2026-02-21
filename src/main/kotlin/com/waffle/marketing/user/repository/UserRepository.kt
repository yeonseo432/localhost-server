package com.waffle.marketing.user.repository

import com.waffle.marketing.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>

    fun existsByUsername(username: String): Boolean
}
