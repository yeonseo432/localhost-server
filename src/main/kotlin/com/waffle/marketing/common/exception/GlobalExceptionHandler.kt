package com.waffle.marketing.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val errorCode: String? = null,
    val validationErrors: Map<String, String?>? = null,
)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(e: ResourceNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Not Found", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Bad Request", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(status = 401, error = "Unauthorized", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(ResourceForbiddenException::class)
    fun handleForbidden(e: ResourceForbiddenException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(status = 403, error = "Forbidden", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(MissionAlreadyCompletedException::class)
    fun handleMissionAlreadyCompleted(e: MissionAlreadyCompletedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(status = 409, error = "Conflict", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(MissionVerificationFailedException::class)
    fun handleVerificationFailed(e: MissionVerificationFailedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ErrorResponse(status = 422, error = "Verification Failed", message = e.message, errorCode = e.errorCode),
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Validation Failed", message = "입력값을 확인해주세요", validationErrors = errors),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(status = 500, error = "Internal Server Error", message = "서버 오류가 발생했습니다"),
        )
}
