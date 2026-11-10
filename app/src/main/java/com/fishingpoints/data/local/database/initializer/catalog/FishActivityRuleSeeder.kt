package com.fishingpoints.data.local.database.initializer.catalog

import android.util.Log
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

@Singleton
class FishActivityRuleSeeder @Inject constructor(
    private val loader: FishActivityRulesLoader,
    private val fishDao: FishDao,
    private val regionDao: RegionDao,
    private val ruleDao: FishActivityRuleDao
) {

    suspend fun seedIfEmpty() {

        Log.d("FishRules", "Начало импорта правил")

        val existingCount = ruleDao.getDefaultRulesCount()

        Log.d("FishRules", "Существующих базовых правил: $existingCount")

        if (existingCount > 0) {
            Log.d("FishRules", "Импорт пропущен: правила уже существуют")
            return
        }

        val data = try {
            loader.load()
        } catch (e: Exception) {
            Log.e("FishRules", "Ошибка чтения JSON", e)
            throw e
        }

        Log.d("FishRules", "Версия JSON: ${data.version}")
        Log.d("FishRules", "Количество правил в JSON: ${data.rules.size}")

        require(data.version > 0)
        require(data.rules.isNotEmpty())

        val rules = data.rules.map { dto ->

            Log.d("FishRules", "Обработка рыбы: ${dto.fishName}")

            val fish = fishDao.getFishByName(dto.fishName)
                ?: throw IllegalStateException(
                    "Рыба не найдена: ${dto.fishName}"
                )

            Log.d("FishRules", "Найдена рыба: ${fish.name}, ID=${fish.id}")

            val regionId = dto.region?.let { regionName ->

                regionDao.getRegionByName(regionName)?.id
                    ?: throw IllegalStateException(
                        "Регион не найден: $regionName"
                    )
            }

            val activity = FeedingActivity.valueOf(dto.activity)

            val season = dto.season?.let {
                Season.valueOf(it)
            }

            val timeOfDay = dto.timeOfDay?.let {
                TimeOfDay.valueOf(it)
            }

            FishActivityRule(
                fishId = fish.id,
                temperatureMin = dto.temperatureMin,
                temperatureMax = dto.temperatureMax,
                season = season,
                timeOfDay = timeOfDay,
                regionId = regionId,
                activity = activity,
                description = dto.description,
                confidence = dto.confidence?.let {
                    RuleConfidence.valueOf(it)
                } ?: RuleConfidence.MEDIUM,
                source = RuleSource.DEFAULT
            )
        }

        Log.d("FishRules", "Подготовлено правил: ${rules.size}")

        ruleDao.insertRules(rules)

        val savedCount = ruleDao.getDefaultRulesCount()

        Log.d("FishRules", "После импорта в базе: $savedCount правил")
    }
}
