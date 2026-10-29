package com.fishingpoints.core.network

import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.ForecastItem
import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.domain.service.BiteRatingCalculator
import com.fishingpoints.domain.utils.DayInfo
import com.fishingpoints.domain.utils.FishingCalendar
import com.fishingpoints.presentation.screens.main.model.BestFishingTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

class FishingService @Inject constructor(
    private val fishingCalendar: FishingCalendar,
    private val biteRatingCalculator: BiteRatingCalculator
) {
    fun getFishingForecast(
        location: LocationEntity,
        date: LocalDate,
        biteRating: Double,
        weather: FishingWeather,
        forecast: ForecastResponse
    ): FishingForecast {

        val dayInfo = fishingCalendar.getDayInfo(
            location = location,
            date = date
        )

        val currentDateTime = fishingCalendar.now(location)

        val timeOfDay =
            fishingCalendar.getTimeOfDay(
                location = location,
                dateTime = currentDateTime
            )

        val season = fishingCalendar.getSeason(date)
        val activityLevel = biteRatingToActivityLevel(biteRating)

        val bestTimes = getBestFishingTimes(
            dayInfo = dayInfo,
            season = season,
            weather = weather,
            forecast = forecast,
            location = location
        )

        return FishingForecast(
            location = location,
            dayInfo = dayInfo,
            timeOfDay = timeOfDay,
            bestTimes = bestTimes,
            activityLevel = activityLevel,
            season = season
        )
    }

    private fun toWeatherForTime(
        item: ForecastItem,
        pressureTrend: PressureTrend
    ): WeatherForTime {
        return WeatherForTime(
            temperature = item.main.temp,
            windSpeed = item.wind.speed,
            cloudiness = item.clouds.all,
            precipitation = item.weather
                .firstOrNull()
                ?.description
                ?: "Нет данных",
            pressureTrend = pressureTrend
        )
    }

    private fun getForecastPointsForDay(
        forecast: ForecastResponse,
        location: LocationEntity,
        date: LocalDate
    ): List<Pair<LocalTime, WeatherForTime>> {

        val allItems = forecast.list
            .map { item ->
                item to getLocalDateTime(item, location)
            }
            .sortedBy { (_, dateTime) ->
                dateTime
            }

        val dayItems = allItems
            .filter { (_, dateTime) ->
                dateTime.toLocalDate() == date
            }

        return dayItems.map { (item, dateTime) ->

            val previousPressure = allItems
                .lastOrNull { (_, previousDateTime) ->
                    previousDateTime.isBefore(dateTime)
                }
                ?.first
                ?.main
                ?.pressure

            val currentPressure = item.main.pressure

            val pressureTrend = when {
                previousPressure == null ->
                    PressureTrend.NODATA

                currentPressure > previousPressure + 2 ->
                    PressureTrend.RISING

                currentPressure < previousPressure - 2 ->
                    PressureTrend.FALLING

                else ->
                    PressureTrend.STABLE
            }

            val weather = toWeatherForTime(
                item = item,
                pressureTrend = pressureTrend
            )

            dateTime.toLocalTime() to weather
        }
    }

    private fun getLocalDateTime(
        item: ForecastItem,
        location: LocationEntity
    ): ZonedDateTime {
        return Instant.ofEpochSecond(item.dt)
            .atZone(location.zoneId)
    }

    private fun getBestFishingTimes(
        dayInfo: DayInfo,
        season: Season,
        weather: FishingWeather,
        forecast: ForecastResponse,
        location: LocationEntity
    ): List<BestFishingTime> {

        val forecastPoints = getForecastPointsForDay(
            forecast = forecast,
            location = location,
            date = dayInfo.date
        )

        if (forecastPoints.isEmpty()) {
            return emptyList()
        }

        // Внутренняя модель кандидата.
        // Сохраняем Double-рейтинг, чтобы не терять точность
        // при сравнении периодов.
        data class Candidate(
            val fishingTime: BestFishingTime,
            val rating: Double
        )

        val candidates = forecastPoints.map { (time, weatherForTime) ->

            // Определяем тип времени суток.
            val timeType = getTimeType(
                time = time,
                dayInfo = dayInfo
            )

            // Рассчитываем рейтинг по погоде конкретного периода.
            val rating = biteRatingCalculator.calculateForecastRating(
                weather = weatherForTime,
                waterTemperature = weather.waterTemperature,
                season = season,
                timeType = timeType
            )

            // Переводим рейтинг в привычную шкалу 0..10.
            val activityLevel = biteRatingToActivityLevel(rating)

            // Находим следующую прогнозную точку.
            val nextTime = forecastPoints
                .firstOrNull { (nextTime, _) -> nextTime > time }
                ?.first

            // Ограничиваем интервал тремя часами.
            val endTime = calculateIntervalEnd(
                start = time,
                nextTime = nextTime
            )

            Candidate(
                fishingTime = BestFishingTime(
                    start = time,
                    end = endTime,
                    activityLevel = activityLevel,
                    description = createDescription(
                        type = timeType,
                        activityLevel = activityLevel
                    ),
                    weather = weatherForTime,
                    timeType = timeType,
                    rating = rating
                ),
                rating = rating
            )
        }

        return candidates
            .sortedWith(
                compareByDescending<Candidate> { it.rating }
                    .thenBy { it.fishingTime.start }
            )
            .take(3)
            .map { it.fishingTime }
            .sortedBy { it.start }
    }

    private fun calculateIntervalEnd(
        start: LocalTime,
        nextTime: LocalTime?
    ): LocalTime {
        val maxDurationSeconds = 3 * 60 * 60

        val maxEnd = start.plusSeconds(maxDurationSeconds.toLong())

        if (nextTime == null) {
            return maxEnd
        }

        val durationSeconds =
            nextTime.toSecondOfDay() - start.toSecondOfDay()

        if (durationSeconds <= 0) {
            return maxEnd
        }

        return if (durationSeconds <= maxDurationSeconds) {
            nextTime
        } else {
            maxEnd
        }
    }


    private fun getTimeType(
        time: LocalTime,
        dayInfo: DayInfo
    ): FishingTimeType {

        val sunrise = dayInfo.sunrise
        val sunset = dayInfo.sunset

        // Рассвет: 60 минут до и после восхода.
        if (isWithinTimeWindow(
                time = time,
                target = sunrise,
                windowMinutes = 60
            )
        ) {
            return FishingTimeType.DAWN
        }

        // Закат: 60 минут до и после захода.
        if (isWithinTimeWindow(
                time = time,
                target = sunset,
                windowMinutes = 60
            )
        ) {
            return FishingTimeType.DUSK
        }

        // Светлое время суток.
        if (time in sunrise..<sunset) {
            return FishingTimeType.DAY
        }

        return FishingTimeType.NIGHT
    }

    /**
     * Проверяет, попадает ли время в интервал вокруг целевого времени.
     *
     * Учитывает переход через полночь.
     */
    private fun isWithinTimeWindow(
        time: LocalTime,
        target: LocalTime,
        windowMinutes: Int
    ): Boolean {

        val timeMinutes = time.toSecondOfDay() / 60
        val targetMinutes = target.toSecondOfDay() / 60

        val difference = Math.floorMod(
            timeMinutes - targetMinutes + 720,
            1440
        ) - 720

        return difference >= -windowMinutes &&
                difference < windowMinutes
    }


    private fun biteRatingToActivityLevel(
        biteRating: Double
    ): Int {

        return (
                biteRating
                    .coerceIn(0.0, 5.0)
                    .div(5.0)
                    .times(10.0)
                    .roundToInt()
                ).coerceIn(0, 10)
    }

    private fun createDescription(
        type: FishingTimeType,
        activityLevel: Int
    ): String {

        return when {
            activityLevel >= 8 ->
                when (type) {
                    FishingTimeType.DAWN -> "🌅 Рассвет — отличное время для рыбалки"
                    FishingTimeType.DUSK -> "🌇 Закат — отличное время для рыбалки"
                    FishingTimeType.DAY -> "☀️ День — высокая активность"
                    FishingTimeType.NIGHT -> "🌙 Ночь — высокая активность"
                }

            activityLevel >= 5 ->
                when (type) {
                    FishingTimeType.DAWN -> "🌅 Рассвет — хорошее время для рыбалки"
                    FishingTimeType.DUSK -> "🌇 Закат — хорошее время для рыбалки"
                    FishingTimeType.DAY -> "☀️ День — умеренная активность"
                    FishingTimeType.NIGHT -> "🌙 Ночь — умеренная активность"
                }

            else ->
                when (type) {
                    FishingTimeType.DAWN -> "🌅 Рассвет — активность снижена"
                    FishingTimeType.DUSK -> "🌇 Закат — активность снижена"
                    FishingTimeType.DAY -> "☀️ День — активность снижена"
                    FishingTimeType.NIGHT -> "🌙 Ночь — активность снижена"
                }
        }
    }

    //
    fun getAllFishingTimes(
        location: LocationEntity,
        date: LocalDate,
        biteRating: Double,
        weather: FishingWeather,
        forecast: ForecastResponse
    ): List<BestFishingTime> {

        val dayInfo = fishingCalendar.getDayInfo(
            location = location,
            date = date
        )

        // Текущее время в часовом поясе выбранной локации.
        val now = fishingCalendar.now(location)

        // Начало прогнозного периода.
        val periodStart = if (now.toLocalDate() == date) {
            now
        } else {
            date.atStartOfDay(location.zoneId)
        }

        // Прогноз на ближайшие 24 часа.
        val periodEnd = periodStart.plusHours(24)

        // Получаем все прогнозные точки с локальными датой и временем.
        val forecastPoints = forecast.list
            .map { item ->
                item to getLocalDateTime(item, location)
            }
            .filter { (_, dateTime) ->
                !dateTime.isBefore(periodStart) &&
                        dateTime.isBefore(periodEnd)
            }
            .sortedBy { (_, dateTime) ->
                dateTime
            }

        if (forecastPoints.isEmpty()) {
            return emptyList()
        }

        return forecastPoints.mapIndexed { index, (item, dateTime) ->

            val time = dateTime.toLocalTime()

            // Для каждой даты получаем её собственные солнечные данные.
            val intervalDayInfo = if (dateTime.toLocalDate() == date) {
                dayInfo
            } else {
                fishingCalendar.getDayInfo(
                    location = location,
                    date = dateTime.toLocalDate()
                )
            }

            // Определяем тип времени суток.
            val timeType = getTimeType(
                time = time,
                dayInfo = intervalDayInfo
            )

            // Находим погодные условия для конкретной точки.
            val previousPressure = forecastPoints
                .take(index)
                .lastOrNull()
                ?.first
                ?.main
                ?.pressure

            val pressureTrend = when {
                previousPressure == null ->
                    PressureTrend.NODATA

                item.main.pressure > previousPressure + 2 ->
                    PressureTrend.RISING

                item.main.pressure < previousPressure - 2 ->
                    PressureTrend.FALLING

                else ->
                    PressureTrend.STABLE
            }

            val weatherForTime = toWeatherForTime(
                item = item,
                pressureTrend = pressureTrend
            )

            // Рассчитываем рейтинг с учётом погоды и времени суток.
            val rating = biteRatingCalculator.calculateForecastRating(
                weather = weatherForTime,
                waterTemperature = weather.waterTemperature,
                season = fishingCalendar.getSeason(dateTime.toLocalDate()),
                timeType = timeType
            ).coerceIn(0.0, 5.0)

            val activityLevel = biteRatingToActivityLevel(rating)

            // Конец интервала — следующая прогнозная точка,
            // но не более чем через 3 часа.
            val nextDateTime = forecastPoints
                .getOrNull(index + 1)
                ?.second

            val intervalEnd = when {
                nextDateTime == null ->
                    minOf(
                        dateTime.plusHours(3),
                        periodEnd
                    )

                else ->
                    minOf(
                        dateTime.plusHours(3),
                        nextDateTime,
                        periodEnd
                    )
            }

            BestFishingTime(
                start = time,
                end = intervalEnd.toLocalTime(),
                activityLevel = activityLevel,
                description = createDescription(
                    type = timeType,
                    activityLevel = activityLevel
                ),
                weather = weatherForTime,
                timeType = timeType,
                rating = rating
            )
        }
    }

}


enum class FishingTimeType(val title: String) {
    DAWN("Рассвет"),
    DAY("День"),
    DUSK("Сумерки"),
    NIGHT("Ночь")
}


data class FishingForecast(
    val location: LocationEntity,
    val dayInfo: DayInfo,
    val timeOfDay: TimeOfDay,
    val bestTimes: List<BestFishingTime>,
    val activityLevel: Int,
    val season: Season
) {
    companion object {
        fun empty(
            location: LocationEntity,
            date: LocalDate
        ): FishingForecast {

            val dayInfo = DayInfo(
                date = date,
                sunrise = LocalTime.MIDNIGHT,
                sunset = LocalTime.MIDNIGHT,
                solarNoon = LocalTime.NOON,
                currentLocalTime = LocalTime.MIDNIGHT,
                dayLength = Duration.ZERO,
                season = Season.WINTER
            )
            return FishingForecast(
                location = location,
                dayInfo = dayInfo,
                timeOfDay = TimeOfDay.NIGHT,
                bestTimes = emptyList(),
                activityLevel = 0,
                season = Season.WINTER
            )
        }
    }

}

data class WeatherForTime(
    val temperature: Double?,
    val windSpeed: Double,
    val cloudiness: Int,
    val precipitation: String,
    val pressureTrend: PressureTrend
)





