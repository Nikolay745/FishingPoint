package com.fishingpoints.data.repository

import androidx.room.withTransaction
import com.fishingpoints.data.local.database.FishingDatabase
import com.fishingpoints.data.local.database.dao.FishActivityRuleDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.database.dto.FishActivityRulesLoader
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow


@Singleton
class FishActivityRuleRepository @Inject constructor(
    private val ruleDao: FishActivityRuleDao
) {


    fun getAllRules(): Flow<List<FishActivityRule>> =
        ruleDao.getAllRules()

    suspend fun getRulesByFishAndRegion(
        fishId: Long,
        regionId: Long?
    ): List<FishActivityRule> =
        ruleDao.getRulesByFishAndRegion(
            fishId = fishId,
            regionId = regionId
        )

    suspend fun getRulesByFishId(
        fishId: Long
    ): List<FishActivityRule> =
        ruleDao.getRulesByFishId(fishId)

    suspend fun insertRule(
        rule: FishActivityRule
    ): Long {

        val error = rule.validate()

        require(error == null) {
            error ?: "Некорректное правило активности"
        }

        require(rule.source == RuleSource.CUSTOM) {
            "Можно добавлять только пользовательские правила"
        }

        return ruleDao.insertRule(rule)
    }

    suspend fun updateRule(
        rule: FishActivityRule
    ) {

        val error = rule.validate()

        require(error == null) {
            error ?: "Некорректное правило активности"
        }

        require(rule.source == RuleSource.CUSTOM) {
            "Базовые правила нельзя редактировать"
        }

        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(
        rule: FishActivityRule
    ) {

        require(rule.source == RuleSource.CUSTOM) {
            "Базовые правила нельзя удалять"
        }

        ruleDao.deleteRule(rule)
    }

    suspend fun getDefaultRulesCount(): Int =
        ruleDao.getDefaultRulesCount()

    fun FishActivityRule.validate(): String? {

        val min = temperatureMin
        val max = temperatureMax

        if (min != null && !min.isFinite()) {
            return "Минимальная температура указана некорректно"
        }

        if (max != null && !max.isFinite()) {
            return "Максимальная температура указана некорректно"
        }

        if (min != null && max != null && min > max) {
            return "Минимальная температура не может быть выше максимальной"
        }

        if (fishId <= 0L) {
            return "Необходимо выбрать рыбу"
        }

        if (regionId != null && regionId <= 0L) {
            return "Некорректный регион"
        }

        return null
    }

}
