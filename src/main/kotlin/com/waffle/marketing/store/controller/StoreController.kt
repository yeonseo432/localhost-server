package com.waffle.marketing.store.controller

import com.waffle.marketing.store.dto.StoreResponse
import com.waffle.marketing.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Store", description = "매장 조회")
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
}
