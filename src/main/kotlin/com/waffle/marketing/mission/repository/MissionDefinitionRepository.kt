package com.waffle.marketing.mission.repository

import com.waffle.marketing.mission.model.MissionDefinition
import com.waffle.marketing.mission.model.MissionType
import org.springframework.data.jpa.repository.JpaRepository

interface MissionDefinitionRepository : JpaRepository<MissionDefinition, Long> {
    fun findByStoreIdAndIsActiveTrue(storeId: Long): List<MissionDefinition>

    fun findByStoreIdAndIsActiveTrueAndType(
        storeId: Long,
        type: MissionType,
    ): List<MissionDefinition>

    fun findByIsActiveTrue(): List<MissionDefinition>

    fun findByIsActiveTrueAndType(type: MissionType): List<MissionDefinition>
}
