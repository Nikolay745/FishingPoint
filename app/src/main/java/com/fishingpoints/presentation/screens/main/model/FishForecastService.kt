package com.fishingpoints.presentation.screens.main.model

import com.fishingpoints.presentation.formatting.formatTimeRange
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.domain.service.BiteRatingCalculator
import com.fishingpoints.domain.utils.FishingCalendar
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class FishForecastService @Inject constructor(
    private val biteRatingCalculator: BiteRatingCalculator,
    private val fishingCalendar: FishingCalendar
) {
    fun calculate(
        fish: Fish,
        weather: FishingWeather,
        season: Season,
        timeOfDay: TimeOfDay,
        location: LocationEntity,
        date: LocalDate,
        allTimes: List<BestFishingTime>
    ): FishForecastRecommendation {

        val reasons = mutableListOf<String>()

        // 1. Температурная пригодность для конкретного вида.
        val temperatureEffect = calculateTemperatureEffect(
            temperature = weather.waterTemperature,
            min = fish.temperatureMin,
            max = fish.temperatureMax
        )

        when {
            weather.waterTemperature == null ->
                reasons += "Температура воды неизвестна"

            temperatureEffect > 0.2 ->
                reasons += "Температура воды благоприятна"

            temperatureEffect < -0.2 ->
                reasons += "Температура воды менее благоприятна"

            else ->
                reasons += "Температура воды допустима"
        }

        // 2. Предпочтения по сезону.
        val preferredSeasons = fish.seasons.orEmpty()

        val seasonEffect = when {
            preferredSeasons.isEmpty() -> 0.0
            season in preferredSeasons -> 0.3
            else -> -0.3
        }

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
        val fishTimes = allTimes.mapNotNull { interval ->

            val intervalWeather = interval.weather
                ?: return@mapNotNull null

            // Используем локальные дату, время и часовой пояс.
            val intervalDateTime = ZonedDateTime.of(
                date,
                interval.start,
                location.zoneId
            )

            val intervalTimeOfDay = fishingCalendar.getTimeOfDay(
                location = location,
                dateTime = intervalDateTime
            )

            // Базовый рейтинг конкретного временного интервала.
            // Погодные условия уже учтены в FishingService.
            var rating = interval.rating

            // Индивидуальная поправка на температурные предпочтения рыбы.
            rating += temperatureEffect

            // Индивидуальная поправка на предпочтения по сезону.
            rating += seasonEffect

            // Предпочтения конкретного вида по времени суток.
            val timeEffect = when {
                preferredTimes.isEmpty() -> 0.0
                intervalTimeOfDay in preferredTimes -> 0.3
                else -> -0.2
            }

            rating += timeEffect
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

            }

            FishTimeForecast(
                start = interval.start,
                end = interval.end,
                rating = rating,
                activityLevel = activityLevel,
                description = interval.description,
                reasons = intervalReasons
            )
        }.sortedByDescending { it.rating }

        // 5. Выбираем три лучших интервала.
        val bestTimes = fishTimes
            .take(3)
            .sortedBy { it.start }

        // 6. Итоговый рейтинг вида — по лучшему доступному интервалу.
        val rating = fishTimes
            .maxOfOrNull { it.rating }
            ?: (
                    weather.biteRating +
                            temperatureEffect +
                            seasonEffect
                    ).coerceIn(0.0, 5.0)

        val activityLevel = (
                rating / 5.0 * 10.0
                ).roundToInt().coerceIn(0, 10)

        // Объясняем итоговый рейтинг.
        when {
            rating >= 4.0 ->
                reasons += "Высокая перспективность клёва"

            rating >= 3.0 ->
                reasons += "Умеренно благоприятные условия"

            rating >= 2.0 ->
                reasons += "Перспективность клёва ниже средней"

            else ->
                reasons += "Условия для клёва неблагоприятны"
        }

        // Объясняем выбор лучшего интервала.
        val bestTime = bestTimes.maxByOrNull { it.rating }

        if (bestTime != null) {
            reasons += "Лучший период: ${formatTimeRange(bestTime.start, bestTime.end)}"
        } else {
            reasons += "Нет доступных временных интервалов с прогнозом"
        }

        // Учитываем предпочтения по времени суток.
        if (preferredTimes.isNotEmpty()) {
            reasons += if (timeOfDay in preferredTimes) {
                "Текущее время суток подходит для этого вида"
            } else {
                "Сейчас не самое предпочтительное время для этого вида"
            }
        }

        if (preferredTimes.isNotEmpty()) {
            reasons += if (timeOfDay in preferredTimes) {
                "Текущее время суток подходит для ловли"
            } else {
                "Для этого вида предпочтительно другое время суток"
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
            bestTimes = bestTimes
        )
    }

    private fun calculateTemperatureEffect(
        temperature: Double?,
        min: Int?,
        max: Int?
    ): Double {

        if (temperature == null) return 0.0

        if (min == null && max == null) return 0.0

        // Температура внутри предпочтительного диапазона.
        if (
            (min == null || temperature >= min) &&
            (max == null || temperature <= max)
        ) {
            return 0.5
        }

        // Расстояние до ближайшей границы диапазона.
        val deviation = when {
            min != null && temperature < min ->
                min - temperature

            max != null && temperature > max ->
                temperature - max

            else -> 0.0
        }

        // Чем дальше температура от диапазона,
        // тем сильнее отрицательная поправка.
        return when {
            deviation <= 2.0 -> -0.2
            deviation <= 5.0 -> -0.4
            deviation <= 10.0 -> -0.7
            else -> -1.0
        }
    }
}