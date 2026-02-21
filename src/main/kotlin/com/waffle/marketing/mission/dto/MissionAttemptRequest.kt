package com.waffle.marketing.mission.dto

import jakarta.validation.constraints.NotNull

/** M1 / M5: 단순 방문 인증 */
data class VisitMissionRequest(
    @field:NotNull val lat: Double?,
    @field:NotNull val lng: Double?,
)

/** M2: 체류 체크인 */
data class DwellCheckinRequest(
    @field:NotNull val lat: Double?,
    @field:NotNull val lng: Double?,
)

/** M2: 체류 체크아웃 */
data class DwellCheckoutRequest(
    @field:NotNull val lat: Double?,
    @field:NotNull val lng: Double?,
)

/** M3/M4: 이미지 업로드는 multipart/form-data로 처리 */
