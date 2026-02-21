package com.waffle.marketing.mission.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.mission.dto.MissionAttemptRequest
import com.waffle.marketing.mission.dto.MissionAttemptResponse
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.model.MissionType
import com.waffle.marketing.mission.service.MissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mission Attempt", description = "미션 판독 및 수행 이력")
@RestController
@RequestMapping("/api/missions")
class MissionController(
    private val missionService: MissionService,
) {
    @Operation(
        summary = "미션 목록 조회",
        description = "storeId, type 모두 optional. 둘 다 생략하면 전체 활성 미션 반환.\n가능한 type 값: TIME_WINDOW, DWELL, RECEIPT, INVENTORY, STAMP",
    )
    @ApiResponse(responseCode = "200", description = "미션 목록 반환")
    @SecurityRequirements // 공개
    @GetMapping
    fun list(
        @RequestParam(required = false) storeId: Long?,
        @RequestParam(required = false) type: MissionType?,
    ): List<MissionDefinitionResponse> = missionService.getAllMissions(storeId, type)

    /**
     * 미션 판독 (단일 엔드포인트).
     * - M1(시간대) / M5(스탬프): 빈 바디 또는 {} 전송
     * - M2(체류): /attempts/checkin, /attempts/checkout 사용
     * - M3(영수증) / M4(재고): { "imageUrl": "https://..." } 전송
     */
    @Operation(
        summary = "미션 판독",
        description =
            "미션 유형별 성공 여부를 판정합니다.\n" +
                "- M1·M5: 빈 바디({}) 전송\n" +
                "- M2: /attempts/checkin, /attempts/checkout 엔드포인트 사용\n" +
                "- M3·M4: { \"imageUrl\": \"업로드된 이미지 URL\" } 전송 (AI 연동 준비 중)",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "판독 결과 반환"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (M2 미션에 이 엔드포인트 사용, imageUrl 누락 등)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "이미 완료된 미션",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/{missionId}/attempts")
    fun attempt(
        @PathVariable missionId: Long,
        @RequestBody(required = false) request: MissionAttemptRequest?,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptMission(auth.userId(), missionId, request ?: MissionAttemptRequest())

    @Operation(summary = "내 미션 수행 이력 조회")
    @GetMapping("/{missionId}/attempts/me")
    fun myAttempts(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): List<MissionAttemptResponse> = missionService.getMyAttempts(auth.userId(), missionId)

    /** M2: 체류 체크인 */
    @Operation(summary = "M2: 체류 체크인", description = "GPS 반경 검증은 프론트엔드에서 처리. 체크인 후 PENDING 상태로 기록됩니다.")
    @PostMapping("/{missionId}/attempts/checkin")
    fun checkin(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.checkin(auth.userId(), missionId)

    /** M2: 체류 체크아웃 */
    @Operation(summary = "M2: 체류 체크아웃", description = "가장 최근 PENDING 체크인에 체크아웃 시간을 기록하고 SUCCESS로 전환합니다.")
    @PostMapping("/{missionId}/attempts/checkout")
    fun checkout(
        @PathVariable missionId: Long,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.checkout(auth.userId(), missionId)

    private fun Authentication.userId(): Long = principal as Long
}
