package com.waffle.marketing.common.time

import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}

@Component
class SystemTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}
