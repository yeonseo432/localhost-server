package com.waffle.marketing.common.exception

sealed class BusinessException(
    override val message: String,
    val errorCode: String,
) : RuntimeException(message)

class ResourceNotFoundException(message: String) :
    BusinessException(message, "RESOURCE_NOT_FOUND")

class BadRequestException(message: String) :
    BusinessException(message, "BAD_REQUEST")

class ResourceForbiddenException(message: String) :
    BusinessException(message, "RESOURCE_FORBIDDEN")

class UnauthorizedException(message: String) :
    BusinessException(message, "UNAUTHORIZED")

class MissionAlreadyCompletedException(missionId: Long) :
    BusinessException("이미 완료된 미션입니다: $missionId", "MISSION_ALREADY_COMPLETED")

class MissionVerificationFailedException(val retryHint: String?) :
    BusinessException("미션 인증에 실패했습니다", "MISSION_VERIFICATION_FAILED")
