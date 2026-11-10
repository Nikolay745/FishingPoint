package com.fishingpoints.presentation.screens.main.model

import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.domain.service.BiteRatingCalculator
import com.fishingpoints.domain.service.FishActivityCalculator
import com.fishingpoints.domain.utils.FishingCalendar
import com.fishingpoints.presentation.formatting.formatTimeRange
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class FishForecastService @Inject constructor(
    private val biteRatingCalculator: BiteRatingCalculator,
    private val fishingCalendar: FishingCalendar,
    private val fishActivityCalculator: FishActivityCalculator
) {
    suspend fun calculate(
        fish: Fish,
        weather: FishingWeather,
        season: Season,
        timeOfDay: TimeOfDay,
        location: LocationEntity,
        date: LocalDate,
        allTimes: List<BestFishingTime>
    ): FishForecastRecommendation {

        val reasons = mutableListOf<String>()
        // 1. Определяем активность рыбы по правилам.
        val activityResult = fishActivityCalculator.calculate(
            fishId = fish.id,
            waterTemperature = weather.waterTemperature,
            season = season,
            timeOfDay = timeOfDay,
            regionId = location.regionId
        )

        activityResult.description?.let { description ->
            reasons += description
        }

        activityResult.confidence?.let { confidence ->
            reasons += when (confidence) {
                RuleConfidence.HIGH ->
                    "Достоверность правила: высокая"

                RuleConfidence.MEDIUM ->
                    "Достоверность правила: средняя"

                RuleConfidence.LOW ->
                    "Достоверность правила: низкая"
            }
        }


        // 1. Температурная пригодность для конкретного вида.
        val temperatureEffect = calculateTemperatureEffect(
            temperature = weather.waterTemperature,
            min = fish.temperatureMin,
            max = fish.temperatureMax
        )

        val temperatureCompatibility =
            calculateTemperatureCompatibility(
                temperature = weather.waterTemperature,
                min = fish.temperatureMin,
                max = fish.temperatureMax
            )

        reasons += when {
            weather.waterTemperature == null ->
                "Температура воды неизвестна"

            temperatureEffect > 0.2 ->
                "Температура воды благоприятна"

            temperatureEffect < -0.2 ->
                "Температура воды менее благоприятна"

            else ->
                "Температура воды допустима"
        }


        // 2. Предпочтения по сезону.
        val preferredSeasons = fish.seasons.orEmpty()

        val seasonEffect = when {
            preferredSeasons.isEmpty() -> 0.0
            season in preferredSeasons -> 0.3
            else -> -0.3
        }
        val activityBonus = when {
            activityResult.activity == null -> 0.0

            activityResult.hasTemperatureMatch ->
                getActivityBonus(activityResult.activity)

            else -> 0.0
        }


        // Если температурные правила существуют,
// но ни одно из них не подошло,
// не считаем активность подтверждённой.
        val activityTemperatureMismatch =
            activityResult.hasTemperatureRules &&
                    !activityResult.hasTemperatureMatch

        if (preferredSeasons.isNotEmpty()) {
            reasons += if (seasonEffect > 0.0) {
                "Текущий сезон подходит для этого вида"
            } else {
                "Текущий сезон не входит в предпочтительные"
            }
        }

        // 3. Предпочтения по времени суток.
        val preferredTimes = fish.bestTimeOfDay.orEmpty()

        // 4. Индивидуально рассчитываем каждый интервал.
        val fishTimes = allTimes.map { interval ->

            // Используем локальные дату, время и часовой пояс.
            val intervalDateTime = ZonedDateTime.of(
                interval.date,
                interval.start,
                location.zoneId
            )

            val intervalTimeOfDay = fishingCalendar.getTimeOfDay(
                location = location,
                dateTime = intervalDateTime
            )

            // Индивидуальная активность рыбы для данного интервала.
            val intervalActivity = fishActivityCalculator.calculate(
                fishId = fish.id,
                waterTemperature = weather.waterTemperature,
                season = season,
                timeOfDay = intervalTimeOfDay,
                regionId = location.regionId
            )


            // Базовый рейтинг конкретного временного интервала.
            // Погодные условия уже учтены в FishingService.
            var rating = interval.rating

            // Индивидуальная поправка на температурные предпочтения рыбы.


            // Предпочтения конкретного вида по времени суток.
            val timeEffect = when {
                preferredTimes.isEmpty() -> 0.0
                intervalTimeOfDay in preferredTimes -> 0.3
                else -> -0.2
            }

            rating += temperatureEffect

            rating += seasonEffect

            if (
                intervalActivity.activity != null &&
                intervalActivity.hasTemperatureMatch
            ) {
                rating += getActivityBonus(
                    intervalActivity.activity
                )
            }


            rating += timeEffect

// Учитываем температурную совместимость.
            rating *= temperatureCompatibility

// Если температурные правила существуют,
// но не найдено подходящего правила активности,
// ограничиваем рейтинг.
            if (
                activityTemperatureMismatch ||
                (
                        intervalActivity.hasTemperatureRules &&
                                !intervalActivity.hasTemperatureMatch
                        )
            ) {
                rating = minOf(rating, 1.5)
            }

            rating = rating.coerceIn(0.0, 5.0)

            val activityLevel = (
                    rating / 5.0 * 10.0
                    ).roundToInt().coerceIn(0, 10)

            val intervalReasons = buildList {
                add(interval.description)

                // Температура воды.
                if (weather.waterTemperature == null) {
                    add("Температура воды неизвестна")
                } else if (
                    fish.temperatureMin != null ||
                    fish.temperatureMax != null
                ) {
                    when {
                        temperatureEffect > 0.2 ->
                            add("Температура воды подходит этому виду")

                        temperatureEffect < -0.2 ->
                            add("Температура воды выходит за предпочтительный диапазон")

                        else ->
                            add("Температура воды не даёт выраженного преимущества")
                    }
                }

                // Сезон.
                if (preferredSeasons.isNotEmpty()) {
                    if (seasonEffect > 0.0) {
                        add("Сезон благоприятен для этого вида")
                    } else {
                        add("Сезон не входит в предпочтительные")
                    }
                }

                // Время суток.
                if (preferredTimes.isNotEmpty()) {
                    if (timeEffect > 0.0) {
                        add("Время суток подходит этому виду")
                    } else {
                        add("Это время суток не входит в предпочтительные")
                    }
                }

                // Индивидуальная активность рыбы.
                if (
                    intervalActivity.activity != null &&
                    intervalActivity.hasTemperatureMatch
                ) {
                    val activity = intervalActivity.activity

                    add(
                        "${activity.icon} Активность рыбы: ${activity.displayName}"
                    )
                }


                intervalActivity.description?.let { description ->
                    add(description)
                }


            }

            FishTimeForecast(
                start = interval.start,
                end = interval.end,
                rating = rating,
                activityLevel = activityLevel,
                description = interval.description,
                reasons = intervalReasons,

                feedingActivity = intervalActivity.activity
                    ?: FeedingActivity.NONE,

                activityDescription = intervalActivity.description,

                ruleConfidence = intervalActivity.confidence
            )

        }.sortedByDescending { it.rating }

        // 5. Выбираем три лучших интервала.
        val bestTimes = fishTimes
            .take(3)
            .sortedBy { it.start }

        // 6. Итоговый рейтинг вида — по лучшему доступному интервалу.
        var rating = fishTimes
            .maxOfOrNull { it.rating }
            ?: (  weather.biteRating +
                            temperatureEffect +
                            seasonEffect +
                            activityBonus
                    ) * temperatureCompatibility

        rating = rating.coerceIn(0.0, 5.0)

        if (activityTemperatureMismatch) {
            rating = minOf(rating, 1.5)
        }





        val activityLevel = (
                rating / 5.0 * 10.0
                ).roundToInt().coerceIn(0, 10)

        activityResult.activity?.let { activity ->
            reasons += "${activity.icon} Активность рыбы: ${activity.displayName}"
        }

        activityResult.description?.let {
            reasons += it
        }


        // Объясняем итоговый рейтинг.
        reasons += when {
            rating >= 4.0 ->
                "Высокая перспективность клёва"

            rating >= 3.0 ->
                "Умеренно благоприятные условия"

            rating >= 2.0 ->
                "Перспективность клёва ниже средней"

            else ->
                "Условия для клёва неблагоприятны"
        }

        // Объясняем выбор лучшего интервала.
        val bestTime = bestTimes.maxByOrNull { it.rating }


        reasons += if (bestTime != null) {
            "Лучший период: ${formatTimeRange(bestTime.start, bestTime.end)}"
        } else {
            "Нет доступных временных интервалов с прогнозом"
        }


        // Учитываем предпочтения по времени суток.
        if (preferredTimes.isNotEmpty()) {
            reasons += if (timeOfDay in preferredTimes) {
                "Текущее время суток подходит для этого вида"
            } else {
                "Сейчас не самое предпочтительное время для этого вида"
            }
        }

        return FishForecastRecommendation(
            fishId = fish.id,
            fishName = fish.name,
            rating = rating,
            activityLevel = activityLevel,
            reasons = reasons,
            bestTimeOfDay = preferredTimes,
            tipsBaits = fish.tipsBaits,
            tackles = fish.tackles,
            baitForFish = fish.baitForFish,
            bestTimes = bestTimes,
            feedingActivity = activityResult.activity ?: FeedingActivity.NONE,

            activityDescription = activityResult.description,

            ruleConfidence = activityResult.confidence

        )
    }

    private fun calculateTemperatureEffect(
        temperature: Double?,
        min: Int?,
        max: Int?
    ): Double {

        if (temperature == null) return 0.0

        if (min == null && max == null) return 0.0

        if (
            (min == null || temperature >= min) &&
            (max == null || temperature <= max)
        ) {
            return 0.5
        }

        val deviation = when {
            min != null && temperature < min ->
                min - temperature

            max != null && temperature > max ->
                temperature - max

            else -> 0.0
        }

        return when {
            deviation <= 2.0 -> -0.3
            deviation <= 5.0 -> -0.7
            deviation <= 8.0 -> -1.2
            deviation <= 12.0 -> -1.8
            deviation <= 15.0 -> -2.5
            else -> -3.0
        }
    }

    private fun calculateTemperatureCompatibility(
        temperature: Double?,
        min: Int?,
        max: Int?
    ): Double {

        // Температура неизвестна.
        if (temperature == null) return 0.5

        // Нет температурных ограничений.
        if (min == null && max == null) return 0.5

        // Температура соответствует предпочтительному диапазону.
        if (
            (min == null || temperature >= min) &&
            (max == null || temperature <= max)
        ) {
            return 1.0
        }

        val deviation = when {
            min != null && temperature < min ->
                min - temperature

            max != null && temperature > max ->
                temperature - max

            else -> 0.0
        }

        return when {
            deviation <= 2.0 -> 0.8
            deviation <= 5.0 -> 0.6
            deviation <= 8.0 -> 0.4
            deviation <= 12.0 -> 0.2
            else -> 0.0
        }
    }

    private fun getActivityBonus(
        activity: FeedingActivity
    ): Double {
        return when (activity) {
            FeedingActivity.VERY_HIGH -> 0.8
            FeedingActivity.HIGH -> 0.5
            FeedingActivity.MEDIUM -> 0.2
            FeedingActivity.LOW -> -0.3
            FeedingActivity.NONE -> 0.0
        }
    }

}