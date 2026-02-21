package com.waffle.marketing.mission.controller

import com.waffle.marketing.mission.dto.MissionAttemptResponse
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.service.MissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Mission", description = "미션 5종 수행")
@RestController
@RequestMapping("/api/missions")
class MissionController(
    private val missionService: MissionService,
) {
    @Operation(summary = "매장별 미션 목록 조회")
    @GetMapping
    fun getByStore(
        @RequestParam storeId: Long,
    ): List<MissionDefinitionResponse> = missionService.getMissionsByStore(storeId)

    /** M1: 특정 시간대 방문 */
    @Operation(summary = "M1: 시간대 방문 인증")
    @PostMapping("/{missionId}/time-window")
    fun attemptTimeWindow(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptTimeWindow(auth.userId(), missionId)

    /** M2: 체류 체크인 */
    @Operation(summary = "M2: 체류 체크인")
    @PostMapping("/{missionId}/dwell/checkin")
    fun checkin(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.checkin(auth.userId(), missionId)

    /** M2: 체류 체크아웃 */
    @Operation(summary = "M2: 체류 체크아웃")
    @PostMapping("/{missionId}/dwell/checkout")
    fun checkout(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.checkout(auth.userId(), missionId)

    /** M3: 영수증 사진 업로드 */
    @Operation(summary = "M3: 영수증 사진 업로드 및 제품 인증")
    @PostMapping("/{missionId}/receipt", consumes = ["multipart/form-data"])
    fun attemptReceipt(
        @PathVariable missionId: Long,
        @RequestParam("image") image: MultipartFile,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptReceipt(auth.userId(), missionId, image)

    /** M4: 재고 사진 업로드 */
    @Operation(summary = "M4: 재고 상품 사진 촬영 인증")
    @PostMapping("/{missionId}/inventory", consumes = ["multipart/form-data"])
    fun attemptInventory(
        @PathVariable missionId: Long,
        @RequestParam("image") image: MultipartFile,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptInventory(auth.userId(), missionId, image)

    /** M5: 반복 방문 스탬프 */
    @Operation(summary = "M5: 반복 방문 스탬프")
    @PostMapping("/{missionId}/stamp")
    fun attemptStamp(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptStamp(auth.userId(), missionId)

    private fun Authentication.userId(): Long = principal as Long
}
