package com.waffle.marketing.mission.repository

import com.waffle.marketing.mission.model.MissionDefinition
import org.springframework.data.jpa.repository.JpaRepository

interface MissionDefinitionRepository : JpaRepository<MissionDefinition, Long> {
    fun findByStoreIdAndIsActiveTrue(storeId: Long): List<MissionDefinition>
}
