package com.waffle.marketing.mission.service

import com.waffle.marketing.common.exception.ExternalApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.time.Duration
import java.util.Base64

@Component
class EliceAiClient(
    @Qualifier("eliceRestClient") private val eliceRestClient: RestClient,
    @Value("\${elice.model}") private val model: String,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val imageDownloadClient: RestClient =
        run {
            val httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
            val factory =
                JdkClientHttpRequestFactory(httpClient).apply {
                    setReadTimeout(Duration.ofSeconds(30))
                }
            RestClient.builder().requestFactory(factory).build()
        }

    fun analyzeReceipt(
        imageBytes: ByteArray,
        configJson: String,
    ): AiJudgmentResult {
        val configNode = objectMapper.readTree(configJson)
        val targetProductKey = configNode.get("targetProductKey").textValue()
        val confidenceThreshold = configNode.get("confidenceThreshold")?.asDouble() ?: 0.7

        val b64Image = Base64.getEncoder().encodeToString(imageBytes)
        val userContent =
            listOf(
                mapOf(
                    "type" to "text",
                    "text" to
                        "Read the receipt in this image and determine if it contains a purchase of the target product.\n" +
                        "Target product: $targetProductKey\n" +
                        "Confidence threshold: $confidenceThreshold",
                ),
                mapOf(
                    "type" to "image_url",
                    "image_url" to mapOf("url" to "data:image/jpeg;base64,$b64Image"),
                ),
            )

        val messages =
            listOf(
                mapOf("role" to "system", "content" to RECEIPT_SYSTEM_PROMPT),
                mapOf("role" to "user", "content" to userContent),
            )

        val rawContent = callEliceApi(messages)
        return parseAiResponse(rawContent, confidenceThreshold)
    }

    fun compareInventory(
        imageBytes: ByteArray,
        configJson: String,
    ): AiJudgmentResult {
        val configNode = objectMapper.readTree(configJson)
        val answerImageUrl = configNode.get("answerImageUrl").textValue()
        val confidenceThreshold = configNode.get("confidenceThreshold")?.asDouble() ?: 0.7

        val b64Image = Base64.getEncoder().encodeToString(imageBytes)
        val answerB64Uri = downloadImageAsBase64(answerImageUrl)

        val userContent =
            listOf(
                mapOf(
                    "type" to "text",
                    "text" to
                        "Compare these two images. The first is the user's photo, " +
                        "the second is the reference product image. " +
                        "Confidence threshold: $confidenceThreshold\n" +
                        "Are they showing the same product?",
                ),
                mapOf(
                    "type" to "image_url",
                    "image_url" to mapOf("url" to "data:image/jpeg;base64,$b64Image"),
                ),
                mapOf(
                    "type" to "image_url",
                    "image_url" to mapOf("url" to answerB64Uri),
                ),
            )

        val messages =
            listOf(
                mapOf("role" to "system", "content" to INVENTORY_SYSTEM_PROMPT),
                mapOf("role" to "user", "content" to userContent),
            )

        val rawContent = callEliceApi(messages)
        return parseAiResponse(rawContent, confidenceThreshold)
    }

    private fun callEliceApi(messages: List<Map<String, Any>>): String {
        val payload =
            mapOf(
                "model" to model,
                "messages" to messages,
                "max_completion_tokens" to 4096,
            )

        try {
            val response =
                eliceRestClient
                    .post()
                    .uri("/v1/chat/completions")
                    .body(payload)
                    .retrieve()
                    .body(Map::class.java)!!

            @Suppress("UNCHECKED_CAST")
            val choices = response["choices"] as List<Map<String, Any>>
            val message = choices[0]["message"] as Map<String, Any>
            return message["content"] as String
        } catch (e: RestClientException) {
            throw ExternalApiException("Elice AI API 호출 실패: ${e.message}")
        }
    }

    private fun parseAiResponse(
        rawContent: String,
        threshold: Double,
    ): AiJudgmentResult {
        val parsed =
            try {
                val cleaned = stripMarkdownFences(rawContent)
                objectMapper.readTree(cleaned)
            } catch (e: Exception) {
                log.warn("AI 응답 파싱 실패: {}", rawContent)
                val fallback = mapOf("match" to false, "confidence" to 0.0, "retryHint" to "AI 응답을 파싱할 수 없습니다. 다시 시도해주세요.")
                val fallbackJson = objectMapper.writeValueAsString(fallback)
                return AiJudgmentResult(
                    match = false,
                    confidence = 0.0,
                    retryHint = "AI 응답을 파싱할 수 없습니다. 다시 시도해주세요.",
                    rawJson = fallbackJson,
                )
            }

        val confidence = parsed.get("confidence")?.asDouble() ?: 0.0
        val aiMatch = parsed.get("match")?.asBoolean() ?: false
        val match = aiMatch && confidence >= threshold
        val retryHint =
            if (match) {
                parsed.get("retryHint")?.textValue()
            } else {
                parsed.get("retryHint")?.textValue() ?: "신뢰도가 기준에 미달합니다. 더 선명한 사진으로 다시 시도해주세요."
            }

        return AiJudgmentResult(
            match = match,
            confidence = confidence,
            retryHint = retryHint,
            rawJson = objectMapper.writeValueAsString(parsed),
        )
    }

    private fun stripMarkdownFences(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed
        val lines = trimmed.split("\n")
        return lines
            .drop(1)
            .filter { !it.trim().startsWith("```") }
            .joinToString("\n")
    }

    private fun downloadImageAsBase64(url: String): String {
        try {
            val response =
                imageDownloadClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(ByteArray::class.java)

            val contentType =
                response.headers.contentType
                    ?.toString()
                    ?.split(";")
                    ?.get(0)
                    ?.trim() ?: "image/jpeg"
            val b64 = Base64.getEncoder().encodeToString(response.body!!)
            return "data:$contentType;base64,$b64"
        } catch (e: RestClientException) {
            throw ExternalApiException("참조 이미지 다운로드 실패: ${e.message}")
        }
    }

    companion object {
        private val RECEIPT_SYSTEM_PROMPT =
            "You are a receipt image analysis assistant. " +
                "You will be given an image of a receipt. Read the receipt directly from the image " +
                "and determine whether the target product name appears in it. " +
                "IMPORTANT RULES:\n" +
                "- Read every line of the receipt image carefully.\n" +
                "- Match by CHARACTER SHAPE SIMILARITY ONLY. Do NOT consider semantic meaning or product categories.\n" +
                "- For example, '돌체라떼' and '돌채라떼' are similar (printing artifacts), " +
                "but '바리스타' and '돌체라떼' are NOT similar even though both are coffee-related.\n" +
                "- NEVER increase confidence based on semantic similarity (same category, related meaning, etc.).\n" +
                "- Only match when the actual characters closely resemble the target product name.\n" +
                "- IGNORE whitespace differences: '코카콜라' and '코카 콜라', '돌체라떼' and '돌체 라떼' should be treated as the same product.\n" +
                "- If the image is too blurry or unreadable, set match to false and provide a helpful retryHint.\n" +
                "Respond ONLY with a JSON object: " +
                """{"match": true/false, "confidence": 0.0-1.0, "retryHint": "string or null", "reason": "brief explanation"}"""

        private val INVENTORY_SYSTEM_PROMPT =
            "You are an inventory verification assistant. " +
                "Compare the user's photo (first image) with the reference product image (second image) " +
                "and determine if they show the SAME product.\n" +
                "IMPORTANT RULES:\n" +
                "- Judge by PRODUCT IDENTITY: same brand, same product name, same packaging design.\n" +
                "- Different flavors, sizes, or variants of the same brand are DIFFERENT products " +
                "(e.g., Coca-Cola Original vs Coca-Cola Zero are different).\n" +
                "- IGNORE differences caused by shooting angle, lighting, background, or image quality.\n" +
                "- If the user's photo is too blurry, too dark, or the product is not clearly visible, " +
                "set match to false and provide a helpful retryHint IN KOREAN.\n" +
                "- The retryHint must always be in Korean (e.g., '제품이 잘 보이도록 다시 촬영해주세요.').\n" +
                "Respond ONLY with a JSON object: " +
                """{"match": true/false, "confidence": 0.0-1.0, "retryHint": "string or null", "reason": "brief explanation"}"""
    }
}
