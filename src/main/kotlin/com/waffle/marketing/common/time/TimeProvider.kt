package com.waffle.marketing.common.time

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val KST = ZoneId.of("Asia/Seoul")

interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate
}

@Component
class SystemTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = ZonedDateTime.now(KST).toLocalDateTime()
    override fun today(): LocalDate = ZonedDateTime.now(KST).toLocalDate()
}
