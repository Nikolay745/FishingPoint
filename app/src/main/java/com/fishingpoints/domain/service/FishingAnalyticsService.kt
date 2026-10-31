package com.fishingpoints.domain.service

import com.fishingpoints.core.common.OPTIMAL_WATER_TEMP_MAX
import com.fishingpoints.data.models.DailyFishingForecast
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.data.models.WeatherResponse
import com.fishingpoints.domain.utils.FishingCalendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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

    fun analyzeFishingForecast(
        forecastResponse: ForecastResponse
    ): List<DailyFishingForecast> {

        return forecastResponse.list
//            .groupBy { item ->
//                Instant.ofEpochSecond(item.dt)
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate()
//            }
            .groupBy { item ->
                Instant.ofEpochSecond(item.dt)
                    .atOffset(
                        ZoneOffset.ofTotalSeconds(
                            forecastResponse.city.timezone
                        )
                    )
                    .toLocalDate()
            }

            .map { (date, items) ->

                // Температура
                val temperatureMin = items.minOf {
                    it.main.tempMin
                }

                val temperatureMax = items.maxOf {
                    it.main.tempMax
                }

                // Берём дневной интервал около 12:00
                val representative = items.minByOrNull { item ->
                    kotlin.math.abs(
                        Instant.ofEpochSecond(item.dt)
                            .atZone(ZoneId.systemDefault())
                            .hour - 12
                    )
                } ?: items.first()

                val weatherId =
                    representative.weather.firstOrNull()?.id ?: 0

                val windSpeed = items.map {
                    it.wind.speed
                }.average()

                val pressure = items.map {
                    it.main.pressure
                }.average().toInt()

                val cloudiness = items.map {
                    it.clouds.all
                }.average().toInt()

                val precipitation =
                    representative.weather.firstOrNull()?.description
                        ?: "Нет данных"

                // Расчётная температура воды
                val waterTemperature =
                    waterTemperatureAnalyzer.estimate(
                        representative.main.temp
                    )

                // Формируем погодные условия
                val weather = FishingWeather(
                    temperature = representative.main.temp,
                    waterTemperature = waterTemperature,
                    isRealWaterTemperature = false,
                    pressure = pressure,
                    pressureTrend = PressureTrend.NODATA,
                    windSpeed = windSpeed,
                    cloudiness = cloudiness,
                    precipitation = precipitation,
                    recommendation = "",
                    biteRating = 0.0,
                    weatherId = weatherId
                )

                // Рассчитываем активность клёва
                val biteRating = biteRatingCalculator.calculate(
                    weather = weather
                )

                val recommendation = generateRecommendation(
                    weather = weather,
                    biteRating = biteRating
                )

                DailyFishingForecast(
                    date = date,
                    temperatureMin = temperatureMin,
                    temperatureMax = temperatureMax,
                    weatherId = weatherId,
                    windSpeed = windSpeed,
                    pressure = pressure,
                    cloudiness = cloudiness,
                    precipitation = precipitation,
                    biteRating = biteRating,
                    recommendation = recommendation
                )
            }
            .sortedBy { it.date }
    }

}
