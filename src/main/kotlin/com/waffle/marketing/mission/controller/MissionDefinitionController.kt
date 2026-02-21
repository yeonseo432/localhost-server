package com.waffle.marketing.mission.controller

import com.waffle.marketing.common.exception.ErrorResponse
import com.waffle.marketing.mission.dto.ImageConfirmRequest
import com.waffle.marketing.mission.dto.MissionCreateRequest
import com.waffle.marketing.mission.dto.MissionDefinitionResponse
import com.waffle.marketing.mission.dto.MissionUpdateRequest
import com.waffle.marketing.mission.dto.PresignedUrlRequest
import com.waffle.marketing.mission.dto.PresignedUrlResponse
import com.waffle.marketing.mission.model.MissionType
import com.waffle.marketing.mission.service.MissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@Tag(name = "Mission Definition", description = "미션 정의 CRUD (매장별)")
@RestController
@RequestMapping("/api/stores/{storeId}/missions")
class MissionDefinitionController(
    private val missionService: MissionService,
) {
    @Operation(
        summary = "매장 미션 목록 조회",
        description = "활성 상태(isActive=true)인 미션만 반환합니다. type 파라미터로 특정 미션 타입만 필터링할 수 있습니다.\n" +
            "가능한 값: TIME_WINDOW, DWELL, RECEIPT, INVENTORY, STAMP",
    )
    @ApiResponse(responseCode = "200", description = "미션 목록 반환")
    @SecurityRequirements // 공개
    @GetMapping
    fun list(
        @PathVariable storeId: Long,
        @RequestParam(required = false) type: MissionType?,
    ): List<MissionDefinitionResponse> = missionService.getMissionsByStore(storeId, type)

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
    @SwaggerRequestBody(
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "M1 TIME_WINDOW",
                        summary = "특정 시간대 방문 — startHour 이상 endHour 미만, days 요일 배열",
                        value = """{"type":"TIME_WINDOW","configJson":{"startHour":15,"endHour":17,"days":["MON"]},"rewardAmount":100}""",
                    ),
                    ExampleObject(
                        name = "M2 DWELL",
                        summary = "매장 체류 — durationMinutes 분 이상 머물면 성공",
                        value = """{"type":"DWELL","configJson":{"durationMinutes":10},"rewardAmount":100}""",
                    ),
                    ExampleObject(
                        name = "M3 RECEIPT",
                        summary = "영수증 인증 — targetProductKey 상품이 영수증에 있으면 성공",
                        value = """{"type":"RECEIPT","configJson":{"targetProductKey":"아메리카노"},"rewardAmount":100}""",
                    ),
                    ExampleObject(
                        name = "M4 INVENTORY",
                        summary = "재고 이미지 비교 — 등록 후 /{missionId}/image/presigned-url 로 답안 이미지 업로드",
                        value = """{"type":"INVENTORY","configJson":{},"rewardAmount":100}""",
                    ),
                    ExampleObject(
                        name = "M5 STAMP",
                        summary = "반복 방문 스탬프 — requiredCount 회 방문하면 성공",
                        value = """{"type":"STAMP","configJson":{"requiredCount":5},"rewardAmount":100}""",
                    ),
                ],
            ),
        ],
    )
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

    @Operation(
        summary = "INVENTORY 답안 이미지 업로드 URL 발급",
        description = "OWNER 전용. 응답의 presignedUrl로 프론트에서 직접 S3 PUT 요청해 이미지를 업로드한 뒤 /image/confirm을 호출한다. (10분 유효)",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "presigned URL 반환"),
        ApiResponse(
            responseCode = "400",
            description = "INVENTORY 미션이 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음 또는 본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/{missionId}/image/presigned-url")
    @PreAuthorize("hasRole('OWNER')")
    fun getPresignedUrl(
        @PathVariable storeId: Long,
        @PathVariable missionId: Long,
        @Valid @RequestBody request: PresignedUrlRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): PresignedUrlResponse = missionService.generateAnswerImagePresignedUrl(storeId, missionId, ownerId, request.contentType)

    @Operation(
        summary = "INVENTORY 답안 이미지 업로드 확인",
        description = "OWNER 전용. 프론트가 S3 업로드 완료 후 호출. 기존 이미지는 S3에서 자동 삭제되고 configJson.answerImageUrl이 갱신된다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "갱신된 미션 반환"),
        ApiResponse(
            responseCode = "400",
            description = "INVENTORY 미션이 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "OWNER 권한 없음 또는 본인 매장 아님",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PutMapping("/{missionId}/image/confirm")
    @PreAuthorize("hasRole('OWNER')")
    fun confirmImageUpload(
        @PathVariable storeId: Long,
        @PathVariable missionId: Long,
        @Valid @RequestBody request: ImageConfirmRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal ownerId: Long,
    ): MissionDefinitionResponse = missionService.confirmAnswerImageUpload(storeId, missionId, ownerId, request.imageUrl)

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
