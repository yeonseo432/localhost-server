package com.waffle.marketing.store.controller

import com.waffle.marketing.store.dto.StoreRequest
import com.waffle.marketing.store.dto.StoreResponse
import com.waffle.marketing.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    @GetMapping
    fun getAll(): List<StoreResponse> = storeService.getAll()

    @Operation(summary = "매장 단건 조회")
    @GetMapping("/{storeId}")
    fun getById(
        @PathVariable storeId: Long,
    ): StoreResponse = storeService.getById(storeId)

    @Operation(summary = "매장 등록 (OWNER 전용)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER')")
    fun create(
        @Valid @RequestBody request: StoreRequest,
        @AuthenticationPrincipal ownerId: Long,
    ): StoreResponse = storeService.create(request, ownerId)
}
