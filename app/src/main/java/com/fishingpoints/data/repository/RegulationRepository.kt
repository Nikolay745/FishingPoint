package com.fishingpoints.data.repository

import com.fishingpoints.core.utils.isActiveOn
import com.fishingpoints.data.local.database.dao.FishingBanDao
import com.fishingpoints.data.local.database.dao.RegionRuleDao
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.RegionRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RegulationRepository @Inject constructor(
    private val regionRuleDao: RegionRuleDao,
    private val fishingBanDao: FishingBanDao
) {

    // =====================================
    // REGION RULE CRUD
    // =====================================

    suspend fun insertRegionRule(
        rule: RegionRule
    ): Long {
        return regionRuleDao.insertRuleRevision(rule)
    }


    // =====================================
    // REGION RULE FLOWS
    // =====================================

    fun getRegionRulesForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<List<RegionRule>> {
        return regionRuleDao.getRegionRulesForDate(
            regionId = regionId,
            date = date.toString()
        )
    }


    // =====================================
    // FISHING BAN CRUD
    // =====================================

    suspend fun insertFishingBan(
        ban: FishingBan
    ): Long {
        return fishingBanDao.insertBan(ban)
    }

    suspend fun updateFishingBan(
        ban: FishingBan
    ) {
        fishingBanDao.update(ban)
    }

    suspend fun deleteFishingBan(
        ban: FishingBan
    ) {
        fishingBanDao.delete(ban)
    }

    // =====================================
    // FISHING BAN FLOWS
    // =====================================

    fun getFishingBansForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<List<FishingBan>> {
        return fishingBanDao.getFishingBansForDate(
            regionId = regionId,
            date = date.toString()
        )
    }

    fun getActiveFishingBans(
        regionId: Long,
        date: LocalDate
    ): Flow<List<FishingBan>> {

        return fishingBanDao
            .getFishingBansForDate(
                regionId = regionId,
                date = date.toString()
            )
            .map { bans ->
                bans.filter { ban ->
                    ban.isActiveOn(date)
                }
            }
    }


    fun getAllRegionRules(
        regionId: Long
    ): Flow<List<RegionRule>> {
        return regionRuleDao.getAllRegionRules(regionId)
    }

    // =====================================
    // СОЗДАНИЕ НОВОГО ПРАВИЛА
    // =====================================

    suspend fun createRegionRule(
        rule: RegionRule
    ): Long {
        return regionRuleDao.createNewRule(rule)
    }

    // =====================================
    // СОЗДАНИЕ НОВОЙ РЕДАКЦИИ
    // =====================================

    suspend fun createRegionRuleRevision(
        previousRule: RegionRule,
        newRule: RegionRule
    ): Long {
        return regionRuleDao.createNewRevision(
            previousRule = previousRule,
            newRule = newRule
        )
    }


    // =====================================
    // ОБНОВЛЕНИЕ РЕДАКЦИИ
    // =====================================

    suspend fun updateRegionRule(
        rule: RegionRule
    ) {
        regionRuleDao.updateRuleRevision(rule)
    }

    // =====================================
    // УДАЛЕНИЕ РЕДАКЦИИ
    // =====================================

    suspend fun deleteRegionRule(
        rule: RegionRule
    ) {
        regionRuleDao.deleteRuleRevision(rule)
    }


    // =====================================
    // ПОЛУЧЕНИЕ ПЕРЕСЕЧЕНИЙ
    // =====================================

    suspend fun getOverlappingRules(
        regionId: Long,
        validFrom: LocalDate,
        validUntil: LocalDate?,
        excludeId: Long
    ): List<RegionRule> {

        return regionRuleDao.getOverlappingRules(
            regionId = regionId,
            validFrom = validFrom.toString(),
            validUntil = validUntil?.toString(),
            excludeId = excludeId
        )
    }



}
