package com.fishingpoints.domain.service

import com.fishingpoints.core.common.HPA_TO_MMHG
import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.PressureTrend
import javax.inject.Inject
import kotlin.math.roundToInt

// Анализатор давления
class PressureAnalyzer @Inject constructor() {
    fun analyze(
        forecast: ForecastResponse
    ): PressureTrend {

        val forecasts = forecast.list.take(8)

        if (forecasts.size < 2) {
            return PressureTrend.NODATA
        }

        val firstPressure =
            convertToMmHg(forecasts.first().main.pressure)

        val lastPressure =
            convertToMmHg(forecasts.last().main.pressure)

        val diff = lastPressure - firstPressure

        return when {
            diff > 2 -> PressureTrend.RISING
            diff < -2 -> PressureTrend.FALLING
            else -> PressureTrend.STABLE
        }
    }

    fun convertToMmHg(
        pressureHPa: Int
    ): Int {
        return (pressureHPa * HPA_TO_MMHG).roundToInt()
    }
}