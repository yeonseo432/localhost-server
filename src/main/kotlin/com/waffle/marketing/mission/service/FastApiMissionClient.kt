package com.waffle.marketing.mission.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

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
        image: MultipartFile,
        configJson: String,
    ): AiJudgmentResult {
        val imageResource =
            object : ByteArrayResource(image.bytes) {
                override fun getFilename(): String = image.originalFilename ?: "receipt.jpg"
            }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("image", imageResource)
        body.add("config", configJson)

        return fastApiRestClient
            .post()
            .uri("/analyze/receipt")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .body(AiJudgmentResult::class.java)!!
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
