package com.fishingpoints.domain.recommendation.adviceToDay

import com.fishingpoints.core.network.FishingForecast
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.domain.lunar.LunarInfo
import com.fishingpoints.presentation.screens.main.model.FishForecastRecommendation
import jakarta.inject.Inject
import jakarta.inject.Singleton



enum class TipPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class FishingTipContext(
    val weather: FishingWeather,
    val lunarInfo: LunarInfo?,
    val fishingForecast: FishingForecast,
    val fish: List<Fish>,
    val fishForecasts: List<FishForecastRecommendation>
)


data class FishingTipResult(
    val title: String,
    val message: String,
    val fishName: String? = null,
    val rating: Double? = null,
    val confidence: ForecastConfidence? = null,
    val recommendedTime: String? = null,
    val recommendedBait: String? = null,
    val recommendedTackle: String? = null,
    val reasons: List<String> = emptyList()
)




@Singleton
class FishingTipEngine @Inject constructor(
    private val confidenceEngine: ForecastConfidenceEngine
) {

    fun generate(
        context: FishingTipContext
    ): FishingTipResult? {

        val weather = context.weather

        // =========================================
        // 1. ПРОВЕРКА ОПАСНЫХ ПОГОДНЫХ УСЛОВИЙ
        // =========================================

        // Гроза.
        if (weather.weatherId in 200..232) {
            return createSafetyTip(
                message = "Сегодня прогнозируются грозовые условия. " +
                        "Не выходите на открытую воду. " +
                        "При приближении грозы прекратите рыбалку " +
                        "и найдите безопасное укрытие."
            )
        }

        // Очень сильный ветер.
        if (weather.windSpeed >= 15.0) {
            return createSafetyTip(
                message = "Сегодня очень сильный ветер. " +
                        "Не выходите на открытую воду. " +
                        "Отложите рыбалку, если условия небезопасны."
            )
        }

        // =========================================
        // 2. ВЫБОР ЛУЧШЕЙ РЫБЫ
        // =========================================

        val bestRecommendation = context.fishForecasts
            .filter { it.rating >= MIN_RECOMMENDED_RATING }
            .maxByOrNull { it.rating }

        if (bestRecommendation == null) {
            return createGeneralTip(context)
        }

        // Находим исходную модель рыбы.
        val fish = context.fish.firstOrNull {
            it.id == bestRecommendation.fishId
        } ?: return createGeneralTip(context)

        // =========================================
        // 3. ФОРМИРОВАНИЕ СТРУКТУРИРОВАННОГО СОВЕТА
        // =========================================

        return createTipResult(
            fish = fish,
            recommendation = bestRecommendation,
            weather = weather
        )
    }

    // =============================================
    // СОЗДАНИЕ СОВЕТА ДЛЯ КОНКРЕТНОЙ РЫБЫ
    // =============================================

    private fun createTipResult(
        fish: Fish,
        recommendation: FishForecastRecommendation,
        weather: FishingWeather
    ): FishingTipResult {

        // Достоверность прогноза.
        val confidence = confidenceEngine.calculate(
            weather = weather,
            recommendation = recommendation
        )

        // Лучший временной интервал.
        val bestTime = recommendation.bestTimes
            .maxByOrNull { it.rating }

        // Причины выбора рыбы.
        val reasons = buildReasons(
            fish = fish,
            recommendation = recommendation,
            weather = weather
        )

        // Дополнительная информация.
        val timeText = bestTime?.let {
            "${it.start}–${it.end}"
        }

        val bait = fish.tipsBaits
            ?.takeIf { it.isNotBlank() }

        val tackle = fish.tackles
            ?.takeIf { it.isNotBlank() }

// Основной текст рекомендации.
        val message = when (recommendation.feedingActivity) {

            FeedingActivity.VERY_HIGH ->
                "Ожидается очень высокая активность питания. " +
                        "Условия благоприятны для рыбалки."

            FeedingActivity.HIGH ->
                "Ожидается высокая активность рыбы. " +
                        "Стоит воспользоваться благоприятными условиями."

            FeedingActivity.MEDIUM ->
                "Активность рыбы умеренная. " +
                        "Попробуйте разные варианты подачи наживки."

            FeedingActivity.LOW ->
                "Активность рыбы снижена. " +
                        "Потребуется более деликатная тактика ловли."

            FeedingActivity.NONE ->
                "Активность питания не определена. " +
                        "Ориентируйтесь на фактические поклёвки."
        }

        return FishingTipResult(
            title = "Совет дня",
            message = message,
            fishName = fish.name,
            rating = recommendation.rating,
            confidence = confidence,
            recommendedTime = timeText,
            recommendedBait = bait,
            recommendedTackle = tackle,
            reasons = reasons
        )

    }

    // =============================================
    // ОБЪЯСНЕНИЕ ВЫБОРА РЫБЫ
    // =============================================

    private fun buildReasons(
        fish: Fish,
        recommendation: FishForecastRecommendation,
        weather: FishingWeather
    ): List<String> = buildList {

        // 1. Рейтинг рыбы.
        add(
            when {
                recommendation.rating >= 4.0 ->
                    "Высокая перспективность клёва"

                recommendation.rating >= 3.0 ->
                    "Умеренно благоприятные условия"

                else ->
                    "Перспективность клёва ниже средней"
            }
        )

        // 2. Температура воды.
        val waterTemperature = weather.waterTemperature

        if (waterTemperature == null) {

            add("Нет данных о температуре воды")

        } else {

            val min = fish.temperatureMin
            val max = fish.temperatureMax

            if (min != null || max != null) {

                val suitable =
                    (min == null || waterTemperature >= min) &&
                            (max == null || waterTemperature <= max)

                add(
                    if (suitable) {
                        "Температура воды соответствует предпочтениям вида"
                    } else {
                        "Температура воды выходит за предпочтительный диапазон"
                    }
                )
            }
        }


        // 4. Ветер.
        if (weather.windSpeed >= 8.0) {
            add("Повышенная скорость ветра")
        }

        // 5. Атмосферное давление.
        if (weather.pressure > 0) {

            add(
                when (weather.pressureTrend) {

                    PressureTrend.FALLING ->
                        "Атмосферное давление снижается"

                    PressureTrend.RISING ->
                        "Атмосферное давление повышается"

                    PressureTrend.STABLE ->
                        "Атмосферное давление стабильно"

                    PressureTrend.NODATA ->
                        "Нет данных об изменении давления"
                }
            )
        }


    }

    // =============================================
    // СОВЕТ ПРИ ОТСУТСТВИИ ПЕРСПЕКТИВНОЙ РЫБЫ
    // =============================================

    private fun createGeneralTip(
        context: FishingTipContext
    ): FishingTipResult {

        val weather = context.weather

        val message = when {

            weather.biteRating < 2.0 ->
                "Сегодня прогноз клёва неблагоприятный. " +
                        "Не задерживайтесь на одной точке без поклёвок. " +
                        "Попробуйте изменить глубину и наживку."

            weather.windSpeed >= 8.0 ->
                "Сегодня ветер достаточно сильный. " +
                        "Выбирайте защищённые участки водоёма " +
                        "и контролируйте положение оснастки."

            weather.waterTemperature != null &&
                    weather.waterTemperature <= 5.0 ->
                "Вода холодная. Активность многих видов рыб " +
                        "может быть снижена. Используйте деликатную подачу."

            else ->
                "Сегодня ориентируйтесь на фактическую активность рыбы. " +
                        "Начните с перспективных участков и проверенных наживок."
        }

        return FishingTipResult(
            title = "Совет дня",
            message = message,
            rating = weather.biteRating,
            confidence = null,
            reasons = listOf(
                "Нет видов рыб с достаточным индивидуальным рейтингом"
            )
        )
    }

    // =============================================
    // СОВЕТ ПО БЕЗОПАСНОСТИ
    // =============================================

    private fun createSafetyTip(
        message: String
    ): FishingTipResult {

        return FishingTipResult(
            title = "⚠️ Важное предупреждение",
            message = message,
            rating = 0.0,
            confidence = null,
            reasons = listOf(
                "Обнаружены потенциально опасные погодные условия"
            )
        )
    }

    companion object {

        /**
         * Минимальный рейтинг рыбы,
         * при котором она может попасть в совет дня.
         */
        private const val MIN_RECOMMENDED_RATING = 3.0
    }
}

