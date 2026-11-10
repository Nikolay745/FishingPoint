package com.fishingpoints.domain.recommendation.adviceToDay

import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.presentation.screens.main.model.FishForecastRecommendation
import jakarta.inject.Inject
import jakarta.inject.Singleton


enum class ForecastConfidenceLevel {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW
}

data class ForecastConfidence(
    val score: Int,
    val level: ForecastConfidenceLevel,
    val reasons: List<String>,
    val availableDataPercent: Int
)


@Singleton
class ForecastConfidenceEngine @Inject constructor() {

    fun calculate(
        weather: FishingWeather,
        recommendation: FishForecastRecommendation
    ): ForecastConfidence {

        var score = 100
        val reasons = mutableListOf<String>()

        // 1. Температура воды.
        if (weather.waterTemperature == null) {
            score -= 20
            reasons += "Нет данных о температуре воды"
        } else if (!weather.isRealWaterTemperature) {
            score -= 10
            reasons += "Используется расчётная температура воды"
        }

        // 2. Атмосферное давление.
        if (weather.pressure <= 0) {
            score -= 15
            reasons += "Нет данных об атмосферном давлении"
        }

        // 3. Скорость ветра.
        if (weather.windSpeed < 0) {
            score -= 10
            reasons += "Некорректные данные о ветре"
        }

        // 4. Индивидуальная активность рыбы.
        when (recommendation.ruleConfidence) {

            RuleConfidence.HIGH -> Unit

            RuleConfidence.MEDIUM -> {
                score -= 10
                reasons += "Средняя достоверность правил активности рыбы"
            }

            RuleConfidence.LOW -> {
                score -= 20
                reasons += "Низкая достоверность правил активности рыбы"
            }

            null -> {
                score -= 15
                reasons += "Нет оценки достоверности правил активности"
            }
        }

        // 5. Наличие временных интервалов.
        if (recommendation.bestTimes.isEmpty()) {
            score -= 20
            reasons += "Нет доступных прогнозных интервалов"
        }

        // 6. Температура воздуха.
        if (weather.temperature == null) {
            score -= 5
            reasons += "Нет данных о температуре воздуха"
        }

        score = score.coerceIn(0, 100)

        val level = when {
            score >= 90 -> ForecastConfidenceLevel.VERY_HIGH
            score >= 75 -> ForecastConfidenceLevel.HIGH
            score >= 55 -> ForecastConfidenceLevel.MEDIUM
            score >= 35 -> ForecastConfidenceLevel.LOW
            else -> ForecastConfidenceLevel.VERY_LOW
        }

        return ForecastConfidence(
            score = score,
            level = level,
            reasons = reasons,
            availableDataPercent = score
        )
    }
}
