package com.waffle.marketing.mission.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.mission.dto.MissionCreateRequest
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.dto.MissionUpdateRequest
import com.waffle.marketing.mission.service.MissionService
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mission Definition", description = "미션 정의 CRUD (매장별)")
@RestController
@RequestMapping("/api/stores/{storeId}/missions")
class MissionDefinitionController(
    private val missionService: MissionService,
) {
    @Operation(summary = "매장 미션 목록 조회", description = "활성 상태(isActive=true)인 미션만 반환합니다.")
    @ApiResponse(responseCode = "200", description = "미션 목록 반환")
    @SecurityRequirements // 공개
    @GetMapping
    fun list(
        @PathVariable storeId: Long,
    ): List<MissionDefinitionResponse> = missionService.getMissionsByStore(storeId)

    @Operation(summary = "미션 단건 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "미션 반환"),
        ApiResponse(
            responseCode = "404",
            description = "미션 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @SecurityRequirements // 공개
    @GetMapping("/{missionId}")
    fun getOne(
        @PathVariable storeId: Long,
        @PathVariable missionId: Long,
    ): MissionDefinitionResponse = missionService.getMissionById(storeId, missionId)

    @Operation(summary = "미션 등록", description = "OWNER 전용. 본인 소유 매장에만 등록 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "미션 등록 성공"),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음 또는 본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER')")
    fun create(
        @PathVariable storeId: Long,
        @Valid @RequestBody request: MissionCreateRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): MissionDefinitionResponse = missionService.createMission(storeId, request, ownerId)

    @Operation(summary = "미션 수정", description = "OWNER 전용. configJson·rewardAmount·isActive 변경 가능. type은 변경 불가.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정된 미션 반환"),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음 또는 본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "미션 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PutMapping("/{missionId}")
    @PreAuthorize("hasRole('OWNER')")
    fun update(
        @PathVariable storeId: Long,
        @PathVariable missionId: Long,
        @Valid @RequestBody request: MissionUpdateRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): MissionDefinitionResponse = missionService.updateMission(storeId, missionId, request, ownerId)

    @Operation(summary = "미션 삭제", description = "OWNER 전용. 소프트 삭제(isActive=false)로 처리됩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음 또는 본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "미션 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{missionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OWNER')")
    fun delete(
        @PathVariable storeId: Long,
        @PathVariable missionId: Long,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ) = missionService.deleteMission(storeId, missionId, ownerId)
}
