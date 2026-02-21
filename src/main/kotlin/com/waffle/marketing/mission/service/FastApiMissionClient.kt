package com.waffle.marketing.mission.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap

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
    /** M3: 영수증 OCR + 제품 매칭 — 이미지 바이트를 직접 FastAPI에 multipart 전송 */
    fun analyzeReceipt(
        imageBytes: ByteArray,
        configJson: String,
    ): AiJudgmentResult {
        val body = buildMultipartBody(imageBytes, "receipt.jpg", configJson)

        return fastApiRestClient
            .post()
            .uri("/analyze/receipt")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .body(AiJudgmentResult::class.java)!!
    }

    /** M4: 재고 이미지 비교 판정 — 이미지 바이트를 직접 FastAPI에 multipart 전송 */
    fun compareInventory(
        imageBytes: ByteArray,
        configJson: String,
    ): AiJudgmentResult {
        val body = buildMultipartBody(imageBytes, "inventory.jpg", configJson)

        return fastApiRestClient
            .post()
            .uri("/analyze/inventory")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .body(AiJudgmentResult::class.java)!!
    }

    private fun buildMultipartBody(
        imageBytes: ByteArray,
        filename: String,
        configJson: String,
    ): LinkedMultiValueMap<String, Any> {
        val imageResource =
            object : ByteArrayResource(imageBytes) {
                override fun getFilename(): String = filename
            }
        val body = LinkedMultiValueMap<String, Any>()
        body.add("image", imageResource)
        body.add("config", configJson)
        return body
    }
}
