package com.waffle.marketing.reward.controller

import com.waffle.marketing.reward.dto.RewardResponse
import com.waffle.marketing.reward.service.RewardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Reward", description = "내 리워드 조회")
@RestController
@RequestMapping("/api/rewards")
class RewardController(
    private val rewardService: RewardService,
) {
    @Operation(summary = "내 리워드 목록 조회")
    @GetMapping("/my")
    fun getMyRewards(auth: Authentication): List<RewardResponse> =
        rewardService.getMyRewards(auth.principal as UUID)
}
