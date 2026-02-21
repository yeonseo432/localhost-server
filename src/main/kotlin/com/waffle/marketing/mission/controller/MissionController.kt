package com.waffle.marketing.mission.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.mission.dto.MissionAttemptResponse
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.model.MissionType
import com.waffle.marketing.mission.service.MissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

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
     * 미션 판독 (JSON 바디 — M1/M5 전용).
     * - M1(시간대) / M5(스탬프): 빈 바디 또는 {} 전송
     * - M2(체류): /attempts/checkin, /attempts/checkout 사용
     * - M3(영수증) / M4(재고): multipart 엔드포인트(아래) 사용
     */
    @Operation(
        summary = "미션 판독 (M1·M5)",
        description =
            "미션 유형별 성공 여부를 판정합니다.\n" +
                "- M1·M5: 빈 바디({}) 전송\n" +
                "- M2: /attempts/checkin, /attempts/checkout 엔드포인트 사용\n" +
                "- M3·M4: multipart/form-data로 이미지를 업로드하세요",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "판독 결과 반환"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (M2/M3/M4 미션에 이 엔드포인트 사용 등)",
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
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptMission(auth.userId(), missionId)

    /**
     * 미션 판독 (multipart 이미지 업로드 — M3/M4 전용).
     * 프론트에서 이미지 파일을 직접 업로드.
     */
    @Operation(
        summary = "미션 판독 (M3·M4 이미지 업로드)",
        description = "M3(영수증)/M4(재고) 미션: multipart/form-data로 이미지 파일을 업로드하여 AI 판독합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "판독 결과 반환"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이미지 미션이 아닌 미션에 사용 등)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "이미 완료된 미션",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/{missionId}/attempts", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun attemptWithImage(
        @PathVariable missionId: Long,
        @RequestPart("image") image: MultipartFile,
        auth: Authentication,
    ): MissionAttemptResponse = missionService.attemptMissionWithImage(auth.userId(), missionId, image.bytes)

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
