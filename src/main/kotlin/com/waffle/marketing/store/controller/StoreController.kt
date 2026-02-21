package com.waffle.marketing.store.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.store.dto.StoreRequest
import com.waffle.marketing.store.dto.StoreResponse
import com.waffle.marketing.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Store", description = "매장 관리")
@RestController
@RequestMapping("/api/stores")
class StoreController(
    private val storeService: StoreService,
) {
    @Operation(summary = "전체 매장 목록 조회")
    @ApiResponse(responseCode = "200", description = "매장 목록 반환")
    @SecurityRequirements // 공개
    @GetMapping
    fun getAll(): List<StoreResponse> = storeService.getAll()

    @Operation(summary = "매장 단건 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "매장 정보 반환"),
        ApiResponse(
            responseCode = "404",
            description = "매장 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @SecurityRequirements // 공개
    @GetMapping("/{storeId}")
    fun getById(
        @PathVariable storeId: Long,
    ): StoreResponse = storeService.getById(storeId)

    @Operation(summary = "내 매장 목록 조회", description = "로그인한 OWNER의 매장 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "내 매장 목록 반환")
    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    fun getMy(
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): List<StoreResponse> = storeService.getMyStores(ownerId)

    @Operation(summary = "매장 등록", description = "OWNER 계정으로 로그인 후 Bearer 토큰을 입력해야 합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "매장 등록 성공"),
        ApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER')")
    fun create(
        @Valid @RequestBody request: StoreRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): StoreResponse = storeService.create(request, ownerId)

    @Operation(summary = "매장 삭제", description = "본인 매장만 삭제 가능합니다. OWNER 계정으로 로그인 후 Bearer 토큰을 입력해야 합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "매장 삭제 성공"),
        ApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "매장 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OWNER')")
    fun delete(
        @PathVariable storeId: Long,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ) = storeService.delete(storeId, ownerId)
}
