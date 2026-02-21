package com.waffle.marketing.mission.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

data class AiJudgmentResult(
    val match: Boolean,
    val confidence: Double,
    val retryHint: String?,
    val rawJson: String,
)

@Component
class FastApiMissionClient(
    @Qualifier("fastApiRestClient") private val fastApiRestClient: RestClient,
) {
    /** M3: 영수증 OCR + 제품 매칭 */
    fun analyzeReceipt(
        imageUrl: String,
        configJson: String,
    ): AiJudgmentResult {
        // TODO: POST /analyze/receipt { imageUrl, configJson }
        throw NotImplementedError("FastAPI 연동 미구현")
    }

    /** M4: 재고 이미지 비교 판정 */
    fun compareInventory(
        imageUrl: String,
        configJson: String,
    ): AiJudgmentResult {
        // TODO: POST /analyze/inventory { imageUrl, configJson }
        throw NotImplementedError("FastAPI 연동 미구현")
    }
}
