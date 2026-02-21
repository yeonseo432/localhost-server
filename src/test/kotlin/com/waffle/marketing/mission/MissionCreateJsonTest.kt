package com.waffle.marketing.mission

import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MissionCreateJsonTest {
    private val objectMapper =
        ObjectMapper
            .builder()
            .addModule(kotlinModule())
            .build()

    data class TestCreateRequest(
        val type: String,
        val configJson: JsonNode,
        val rewardAmount: Int,
        val isActive: Boolean = true,
    )

    @Test
    fun `M4 INVENTORY configJson toString produces valid JSON`() {
        val requestBody =
            """
            {
                "type": "INVENTORY",
                "configJson": {"answerImageUrl": "https://bucket.s3.ap-southeast-2.amazonaws.com/stores/1/inventory/abc123"},
                "rewardAmount": 1000
            }
            """.trimIndent()

        val request = objectMapper.readValue(requestBody, TestCreateRequest::class.java)

        // createMission에서 사용하는 것과 동일한 호출
        val configJsonStr = request.configJson.toString()
        println("=== configJson.toString() = $configJsonStr")

        // toString()이 유효한 JSON을 생성하는지 확인
        val node = objectMapper.readTree(configJsonStr)
        val answerImageUrl = node.get("answerImageUrl")
        assertNotNull(answerImageUrl, "answerImageUrl should not be null")

        val urlValue = answerImageUrl.textValue()
        assertEquals("https://bucket.s3.ap-southeast-2.amazonaws.com/stores/1/inventory/abc123", urlValue)

        // isActive 기본값 확인
        assertTrue(request.isActive, "isActive should default to true")
        assertEquals("INVENTORY", request.type)
    }

    @Test
    fun `M3 RECEIPT works for comparison`() {
        val requestBody =
            """
            {
                "type": "RECEIPT",
                "configJson": {"targetProductKey": "아메리카노"},
                "rewardAmount": 100
            }
            """.trimIndent()

        val request = objectMapper.readValue(requestBody, TestCreateRequest::class.java)
        val configJsonStr = request.configJson.toString()
        println("=== RECEIPT configJson.toString() = $configJsonStr")

        val node = objectMapper.readTree(configJsonStr)
        val targetProductKey = node.get("targetProductKey")
        assertNotNull(targetProductKey)
        assertEquals("아메리카노", targetProductKey.textValue())
    }
}
