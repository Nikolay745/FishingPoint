package com.fishingpoints.domain.service

import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.repository.FishActivityRuleRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class FishActivityResult(
    val activity: FeedingActivity?,
    val description: String?,
    val matchedRule: FishActivityRule?,
    val confidence: RuleConfidence?,
    val hasTemperatureMatch: Boolean,
    val estimateType: ActivityEstimateType = ActivityEstimateType.NONE,
    val hasTemperatureRules: Boolean = false
)


@Singleton
class FishActivityCalculator @Inject constructor(
    private val ruleRepository: FishActivityRuleRepository
) {
    suspend fun calculate(
        fishId: Long,
        waterTemperature: Double?,
        season: Season?,
        timeOfDay: TimeOfDay?,
        regionId: Long?
    ): FishActivityResult {

        // 1. Получаем правила для рыбы и региона.
        val rules = ruleRepository.getRulesByFishAndRegion(
            fishId = fishId,
            regionId = regionId
        )
        // Проверяем, существуют ли температурные правила.
        val hasTemperatureRules = rules.any { rule ->
            rule.temperatureMin != null ||
                    rule.temperatureMax != null
        }


        if (rules.isEmpty()) {
            return noResult(
                "Нет данных об активности этой рыбы."
            )
        }

        // Защита от некорректной температуры.
        val validTemperature = waterTemperature?.takeIf {
            it.isFinite()
        }

        // =====================================================
        // 2. Ищем точные правила
        // =====================================================

        val exactRules = rules.filter { rule ->

            isExactRule(
                rule = rule,
                waterTemperature = validTemperature,
                season = season,
                timeOfDay = timeOfDay,
                regionId = regionId
            )
        }


        // =====================================================
        // 3. Если найдены точные правила
        // =====================================================

        if (exactRules.isNotEmpty()) {

            val selectedRule = selectBestRule(
                rules = exactRules,
                regionId = regionId
            ) ?: return noResult(
                "Не удалось выбрать подходящее правило активности."
            )


            return FishActivityResult(
                activity = selectedRule?.activity,

                description = selectedRule?.description
                    ?: "Найдено подходящее правило активности для текущих условий.",

                matchedRule = selectedRule,

                confidence = selectedRule?.confidence,

                hasTemperatureMatch = hasTemperatureMatch(
                    rule = selectedRule,
                    waterTemperature = validTemperature,

                ),
                hasTemperatureRules = hasTemperatureRules,

                estimateType = ActivityEstimateType.RULE_MATCHED
            )
        }

        // =====================================================
        // 4. Ищем предварительные правила
        // =====================================================

        val preliminaryRules = rules.filter { rule ->

            isPreliminaryRule(
                rule = rule,
                waterTemperature = validTemperature,
                regionId = regionId
            )
        }

        // =====================================================
        // 5. Выбираем лучшее предварительное правило
        // =====================================================

        val preliminaryRule = preliminaryRules
            .takeIf { it.isNotEmpty() }
            ?.let {
                selectBestRule(
                    rules = it,
                    regionId = regionId
                )
            }

        // Предварительное правило
        if (preliminaryRule != null) {

            return FishActivityResult(
                activity = preliminaryRule.activity,
                description = preliminaryRule.description?: "Активность рассчитана по общему температурному правилу.",
                matchedRule = preliminaryRule,
                confidence = RuleConfidence.LOW,
                hasTemperatureMatch = true,
                estimateType = ActivityEstimateType.PRELIMINARY,
                hasTemperatureRules = hasTemperatureRules

            )
        }

        // =====================================================
        // 6. Подходящих правил нет
        // =====================================================

        return noResult(
            when {
                validTemperature == null ->
                    "Нет корректной температуры воды и подходящего общего правила."

                else ->
                    "Нет подходящих правил для текущих условий."

            }
        )
    }


// ============================================================
// REGION MATCH
// ============================================================

    private fun regionMatches(
        rule: FishActivityRule,
        regionId: Long?
    ): Boolean {

        return rule.regionId == null ||
                rule.regionId == regionId
    }


// ============================================================
// TEMPERATURE MATCH
// ============================================================
private fun temperatureMatches(
    rule: FishActivityRule,
    waterTemperature: Double?
): Boolean {

    val min = rule.temperatureMin
    val max = rule.temperatureMax

    // Нет температурных ограничений.
    if (min == null && max == null) {
        return true
    }

    // Для температурного правила температура обязательна.
    val temperature = waterTemperature
        ?.takeIf { it.isFinite() }
        ?: return false

    // Проверяем корректность границ.
    if (!isTemperatureRangeValid(rule)) {
        return false
    }

    // Проверяем минимальную границу.
    if (min != null && temperature < min) {
        return false
    }

    // Проверяем максимальную границу.
    if (max != null && temperature > max) {
        return false
    }

    return true
}




// ============================================================
// TEMPERATURE MATCH INFORMATION
// ============================================================

    private fun hasTemperatureMatch(
        rule: FishActivityRule?,
        waterTemperature: Double?
    ): Boolean {

        val hasTemperatureRange =
            rule?.temperatureMin != null ||
                    rule?.temperatureMax != null

        if (!hasTemperatureRange) {
            return false
        }

        return temperatureMatches(
            rule = rule,
            waterTemperature = waterTemperature
        )
    }


// ============================================================
// TEMPERATURE RANGE VALIDATION
// ============================================================

    private fun isTemperatureRangeValid(
        rule: FishActivityRule
    ): Boolean {

        val min = rule.temperatureMin
        val max = rule.temperatureMax

        if (min != null && !min.isFinite()) {
            return false
        }

        if (max != null && !max.isFinite()) {
            return false
        }

        if (min != null && max != null && min > max) {
            return false
        }

        return true
    }


// ============================================================
// SEASON MATCH
// ============================================================

    private fun seasonMatches(
        rule: FishActivityRule,
        season: Season?
    ): Boolean {

        return rule.season == null ||
                rule.season == Season.ALL_YEAR ||
                rule.season == season
    }


// ============================================================
// TIME OF DAY MATCH
// ============================================================

    private fun timeMatches(
        rule: FishActivityRule,
        timeOfDay: TimeOfDay?
    ): Boolean {

        return rule.timeOfDay == null ||
                rule.timeOfDay == TimeOfDay.ANY ||
                rule.timeOfDay == timeOfDay
    }


// ============================================================
// PRELIMINARY RULE
// ============================================================

private fun isPreliminaryRule(
    rule: FishActivityRule,
    waterTemperature: Double?,
    regionId: Long?
): Boolean {

    val isGeneralSeason =
        rule.season == null ||
                rule.season == Season.ALL_YEAR

    val isGeneralTime =
        rule.timeOfDay == null ||
                rule.timeOfDay == TimeOfDay.ANY

    val hasTemperatureRange =
        rule.temperatureMin != null ||
                rule.temperatureMax != null

    val hasValidTemperature =
        waterTemperature != null &&
                waterTemperature.isFinite()

    if (!hasValidTemperature) return false

    if (!isGeneralSeason) return false

    if (!isGeneralTime) return false

    if (!hasTemperatureRange) return false

    if (!regionMatches(rule, regionId)) return false

    return temperatureMatches(
        rule = rule,
        waterTemperature = waterTemperature
    )
}


// ============================================================
// SELECT BEST RULE
// ============================================================

    private fun selectBestRule(
        rules: List<FishActivityRule>,
        regionId: Long?
    ): FishActivityRule? {

        return rules.maxWithOrNull(

            compareBy<FishActivityRule> {

                // 1. Региональное правило.
                if (
                    it.regionId != null &&
                    it.regionId == regionId
                ) 1 else 0

            }.thenBy {

                // 2. Специфичность условий.
                calculateRuleScore(it)

            }.thenBy {

                // 3. Пользовательское правило.
                if (it.source == RuleSource.CUSTOM) 1 else 0

            }.thenBy {

                // 4. Достоверность.
                confidenceScore(it.confidence)

            }.thenByDescending {

                // 5. Предпочтение более узкого
                // температурного диапазона.
                temperatureRangeWidth(it)

            }.thenByDescending {

                // 6. Детерминированный выбор:
                // при полном равенстве выбираем меньший ID.
                -it.id
            }
        )
    }

    private fun temperatureRangeWidth(
        rule: FishActivityRule
    ): Double {

        val min = rule.temperatureMin
        val max = rule.temperatureMax

        if (min == null || max == null) {
            return Double.POSITIVE_INFINITY
        }

        return max - min
    }



// ============================================================
// RULE SCORE
// ============================================================

    private fun calculateRuleScore(
        rule: FishActivityRule
    ): Int {

        var score = 0

        // Ограничение по температуре.
        if (
            rule.temperatureMin != null ||
            rule.temperatureMax != null
        ) {
            score += 1
        }

        // Конкретный сезон.
        if (
            rule.season != null &&
            rule.season != Season.ALL_YEAR
        ) {
            score += 1
        }

        // Конкретное время суток.
        if (
            rule.timeOfDay != null &&
            rule.timeOfDay != TimeOfDay.ANY
        ) {
            score += 1
        }

        return score
    }



// ============================================================
// CONFIDENCE SCORE
// ============================================================

    private fun confidenceScore(
        confidence: RuleConfidence
    ): Int {

        return when (confidence) {
            RuleConfidence.HIGH -> 3
            RuleConfidence.MEDIUM -> 2
            RuleConfidence.LOW -> 1
        }
    }


// ============================================================
// NO RESULT
// ============================================================

    private fun noResult(
        message: String,
        hasTemperatureRules: Boolean = false
    ): FishActivityResult {

        return FishActivityResult(
            activity = null,
            description = message,
            matchedRule = null,
            confidence = null,
            hasTemperatureMatch = false,
            estimateType = ActivityEstimateType.NONE,
            hasTemperatureRules = hasTemperatureRules
        )
    }


    private fun isExactRule(
        rule: FishActivityRule,
        waterTemperature: Double?,
        season: Season?,
        timeOfDay: TimeOfDay?,
        regionId: Long?
    ): Boolean {

        val hasSeasonSpecificity =
            rule.season != null &&
                    rule.season != Season.ALL_YEAR

        val hasTimeSpecificity =
            rule.timeOfDay != null &&
                    rule.timeOfDay != TimeOfDay.ANY

        val hasTemperatureRange =
            rule.temperatureMin != null ||
                    rule.temperatureMax != null

        val hasSpecificConditions =
            hasSeasonSpecificity ||
                    hasTimeSpecificity ||
                    !hasTemperatureRange

        return hasSpecificConditions &&
                regionMatches(rule, regionId) &&
                temperatureMatches(rule, waterTemperature) &&
                seasonMatches(rule, season) &&
                timeMatches(rule, timeOfDay)
    }


}

enum class ActivityEstimateType {
    RULE_MATCHED,
    PRELIMINARY,
    NONE
}
