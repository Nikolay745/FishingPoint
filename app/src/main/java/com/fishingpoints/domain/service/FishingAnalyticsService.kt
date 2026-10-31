package com.fishingpoints.domain.service

import com.fishingpoints.core.common.OPTIMAL_WATER_TEMP_MAX
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.data.models.WeatherResponse
import com.fishingpoints.domain.utils.FishingCalendar
import java.time.LocalDate
import javax.inject.Inject

class FishingAnalyticsService @Inject constructor(
    private val biteRatingCalculator: BiteRatingCalculator,
    private val pressureAnalyzer: PressureAnalyzer,
    private val waterTemperatureAnalyzer: WaterTemperatureAnalyzer,
    private val fishingCalendar: FishingCalendar
) {
    fun analyzeFishingWeather(
        currentResponse: WeatherResponse,
        forecastResponse: ForecastResponse,
        useRealWaterTemp: Boolean,
        realWaterTemp: Double?,
        date: LocalDate
    ): FishingWeather {
        val current = currentResponse.current
        val pressureTrend = pressureAnalyzer.analyze(forecastResponse)
        val pressure = pressureAnalyzer.convertToMmHg(current.pressure)
        val hasRealWaterTemperature = useRealWaterTemp && realWaterTemp != null
        val waterTemperature =
            if (hasRealWaterTemperature) {
                realWaterTemp
            } else {
                waterTemperatureAnalyzer.estimate(current.temp)
            }

        val weather = FishingWeather(
            temperature = current.temp,
            waterTemperature = waterTemperature,
            isRealWaterTemperature = hasRealWaterTemperature,
            pressure = pressure,
            pressureTrend = pressureTrend,
            windSpeed = current.windSpeed,
            cloudiness = current.clouds,
            precipitation = current.weather
                .firstOrNull()
                ?.description
                ?: "Нет данных",
            recommendation = "",
            biteRating = 0.0,
            weatherId = current.weather.firstOrNull()?.id ?: 0

        )

        val season =
            fishingCalendar.getSeason(date)


        val biteRating = biteRatingCalculator.calculate(
            weather = weather
        )

        val recommendation =
            generateRecommendation(
                weather = weather,
                biteRating = biteRating
            )

        return weather.copy(
            biteRating = biteRating,
            recommendation = recommendation
        )
    }

    private fun generateRecommendation(
        weather: FishingWeather,
        biteRating: Double
    ): String {

        val parts = mutableListOf<String>()

        when {
            biteRating >= 4.5 -> parts.add("🎯 Отличный клев! Самое время на рыбалку")
            biteRating >= 3.5 -> parts.add("👍 Хороший клев")
            biteRating >= 2.5 -> parts.add("👌 Средний клев")
            biteRating >= 1.5 -> parts.add("⚠️ Слабый клев")
            else -> parts.add("❌ Клев плохой")
        }

        weather.waterTemperature?.let { waterTemp ->

            when {
                waterTemp < 10.0 -> parts.add("❄️ Вода холодная")
                waterTemp <= OPTIMAL_WATER_TEMP_MAX -> parts.add("✅ Оптимальная температура воды")
                else -> parts.add("🔥 Вода слишком теплая")
            }
        }

        when (weather.pressureTrend) {
            PressureTrend.FALLING -> parts.add("📉 Давление снижается — условия могут быть благоприятными" )
            PressureTrend.RISING -> parts.add(" Давление растет — клев может ухудшаться" )
            PressureTrend.STABLE -> parts.add("➡️ Давление стабильно")
            PressureTrend.NODATA -> Unit
        }

        when {
            weather.windSpeed < 3 ->  parts.add("🍃 Слабый ветер")
            weather.windSpeed < 7 ->  parts.add("🌬️ Умеренный ветер")
            weather.windSpeed < 12 -> parts.add("💨 Сильный ветер")
            else -> parts.add("⚠️ Очень сильный ветер")
        }

        return parts.joinToString(". ")
    }
}
