package com.fishingpoints.domain.service

import com.fishingpoints.core.network.FishingTimeType
import com.fishingpoints.core.network.WeatherForTime
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import javax.inject.Inject

class BiteRatingCalculator @Inject constructor() {

    companion object {
        private const val BASE_RATING = 2.5
        private const val MIN_RATING = 0.0
        private const val MAX_RATING = 5.0
    }

    /**
     * Рейтинг текущих погодных условий.
     * Результат: 0..5.
     */
    fun calculate(
        weather: FishingWeather
    ): Double {

        return calculateBaseRating(
            airTemperature = weather.temperature,
            waterTemperature = weather.waterTemperature,
            pressureTrend = weather.pressureTrend,
            windSpeed = weather.windSpeed,
            cloudiness = weather.cloudiness,
            precipitation = weather.precipitation
        )
    }

    /**
     * Рейтинг погодных условий для прогнозной точки
     * с учётом времени суток и сезона.
     * Результат: 0..5.
     */
    fun calculateForecastRating(
        weather: WeatherForTime,
        waterTemperature: Double?,
        season: Season,
        timeType: FishingTimeType
    ): Double {

        val baseRating = calculateBaseRating(
            airTemperature = weather.temperature,
            waterTemperature = waterTemperature,
            pressureTrend = weather.pressureTrend,
            windSpeed = weather.windSpeed,
            cloudiness = weather.cloudiness,
            precipitation = weather.precipitation
        )

        val timeModifier = calculateTimeModifier(
            season = season,
            timeType = timeType
        )

        return (baseRating * timeModifier)
            .coerceIn(MIN_RATING, MAX_RATING)
    }

    /**
     * Единый расчёт погодных условий.
     * Используется как для текущей погоды,
     * так и для будущих прогнозных точек.
     */
    private fun calculateBaseRating(
        airTemperature: Double?,
        waterTemperature: Double?,
        pressureTrend: PressureTrend,
        windSpeed: Double,
        cloudiness: Int,
        precipitation: String
    ): Double {

        var score = BASE_RATING

        score += calculateWaterTemperatureEffect(
            waterTemperature
        )

        score += calculateAirTemperatureEffect(
            airTemperature
        )

        score += calculatePressureEffect(
            pressureTrend
        )

        score += calculateWindEffect(
            windSpeed
        )

        score += calculateCloudinessEffect(
            cloudiness
        )

        score += calculatePrecipitationEffect(
            precipitation
        )

        return score.coerceIn(
            MIN_RATING,
            MAX_RATING
        )
    }

    // ============================================================
    // ТЕМПЕРАТУРА ВОДЫ
    // ============================================================

    private fun calculateWaterTemperatureEffect(
        waterTemperature: Double?
    ): Double {

        return when {
            waterTemperature == null -> 0.0

            waterTemperature < 5.0 -> -0.8
            waterTemperature < 10.0 -> -0.3

            waterTemperature <= 20.0 -> 0.5
            waterTemperature <= 24.0 -> 0.2
            waterTemperature <= 28.0 -> -0.3

            else -> -0.8
        }
    }

    // ============================================================
    // ТЕМПЕРАТУРА ВОЗДУХА
    // ============================================================

    private fun calculateAirTemperatureEffect(
        temperature: Double?
    ): Double {

        return when {
            temperature == null -> 0.0

            temperature < -10.0 -> -0.5
            temperature < 0.0 -> -0.2

            temperature <= 20.0 -> 0.2
            temperature <= 28.0 -> 0.1

            else -> -0.3
        }
    }

    // ============================================================
    // АТМОСФЕРНОЕ ДАВЛЕНИЕ
    // ============================================================

    private fun calculatePressureEffect(
        pressureTrend: PressureTrend
    ): Double {

        return when (pressureTrend) {
            PressureTrend.FALLING -> 0.3
            PressureTrend.STABLE -> 0.1
            PressureTrend.RISING -> -0.2
            PressureTrend.NODATA -> 0.0
        }
    }

    // ============================================================
    // ВЕТЕР
    // ============================================================

    private fun calculateWindEffect(
        windSpeed: Double
    ): Double {

        return when {
            windSpeed < 2.0 -> -0.1
            windSpeed < 5.0 -> 0.2
            windSpeed < 8.0 -> 0.1
            windSpeed < 12.0 -> -0.3

            else -> -0.8
        }
    }

    // ============================================================
    // ОБЛАЧНОСТЬ
    // ============================================================

    private fun calculateCloudinessEffect(
        cloudiness: Int
    ): Double {

        return when {
            cloudiness < 20 -> -0.1
            cloudiness < 50 -> 0.0
            cloudiness < 80 -> 0.2

            else -> -0.1
        }
    }

    // ============================================================
    // ОСАДКИ
    // ============================================================

    private fun calculatePrecipitationEffect(
        precipitation: String
    ): Double {

        val value = precipitation
            .trim()
            .lowercase()

        return when {
            value.containsAny(
                "ливень",
                "thunderstorm",
                "heavy intensity rain",
                "very heavy rain",
                "extreme rain"
            ) -> -0.6

            value.containsAny(
                "сильный дождь",
                "heavy rain"
            ) -> -0.5

            value.containsAny(
                "дождь",
                "rain",
                "drizzle",
                "морось"
            ) -> 0.1

            value.containsAny(
                "снег",
                "snow",
                "sleet"
            ) -> -0.2

            else -> 0.0
        }
    }

    private fun String.containsAny(
        vararg values: String
    ): Boolean {
        return values.any { value ->
            value in this
        }
    }

    // ============================================================
    // ПОПРАВКА НА ВРЕМЯ СУТОК И СЕЗОН
    // ============================================================

    private fun calculateTimeModifier(
        season: Season,
        timeType: FishingTimeType
    ): Double {

        return when (timeType) {

            FishingTimeType.DAWN -> {
                when (season) {
                    Season.SPRING -> 1.05
                    Season.SUMMER -> 1.10
                    Season.AUTUMN -> 1.10
                    Season.WINTER -> 0.90
                    Season.ALL_YEAR -> 1.05
                }
            }

            FishingTimeType.DUSK -> {
                when (season) {
                    Season.SPRING -> 1.00
                    Season.SUMMER -> 1.10
                    Season.AUTUMN -> 1.05
                    Season.WINTER -> 0.85
                    Season.ALL_YEAR -> 1.00
                }
            }

            FishingTimeType.DAY -> 0.70

            FishingTimeType.NIGHT -> 0.50
        }
    }

    fun calculateTimeAdjustedRating(
        baseRating: Double,
        weather: WeatherForTime,
        season: Season,
        timeType: FishingTimeType
    ): Double {

        val timeModifier = calculateTimeModifier(
            season = season,
            timeType = timeType
        )

        val weatherModifier = calculateWeatherTimeModifier(
            weather = weather,
            timeType = timeType
        )

        return (
                baseRating * timeModifier * weatherModifier
                ).coerceIn(0.0, 5.0)
    }
    private fun calculateWeatherTimeModifier(
        weather: WeatherForTime,
        timeType: FishingTimeType
    ): Double {

        var modifier = 1.0

        // Облачность влияет на рассвет и закат.
        if (
            timeType == FishingTimeType.DAWN ||
            timeType == FishingTimeType.DUSK
        ) {
            modifier *= when {
                weather.cloudiness < 20 -> 0.97
                weather.cloudiness < 50 -> 1.00
                weather.cloudiness < 80 -> 1.04
                else -> 1.01
            }
        }

        // Ветер.
        modifier *= when {
            weather.windSpeed < 3.0 -> 0.98
            weather.windSpeed < 6.0 -> 1.02
            weather.windSpeed < 9.0 -> 1.00
            weather.windSpeed < 12.0 -> 0.92
            else -> 0.75
        }

        // Изменение давления.
        modifier *= when (weather.pressureTrend) {
            PressureTrend.FALLING -> 1.03
            PressureTrend.STABLE -> 1.00
            PressureTrend.RISING -> 0.97
            PressureTrend.NODATA -> 1.00
        }

        // Осадки.
        val precipitation = weather.precipitation.lowercase()

        modifier *= when {
            "ливень" in precipitation -> 0.75
            "сильный дождь" in precipitation -> 0.82
            "дождь" in precipitation -> 0.95
            "морось" in precipitation -> 1.00
            "снег" in precipitation -> 0.90
            else -> 1.00
        }

        return modifier.coerceIn(0.70, 1.10)
    }
}
