package com.waffle.marketing.common.extension

import com.waffle.marketing.common.exception.ResourceNotFoundException

fun <T> T?.ensureNotNull(message: String = "데이터가 존재하지 않습니다"): T = this ?: throw ResourceNotFoundException(message)
