package com.fishingpoints.presentation.screens.main.model

import com.fishingpoints.core.network.FishingTimeType
import com.fishingpoints.core.network.WeatherForTime
import java.time.LocalTime

data class BestFishingTime(
    val start: LocalTime,
    val end: LocalTime,
    val activityLevel: Int,
    val description: String,
    val weather: WeatherForTime? = null,
    val timeType: FishingTimeType = FishingTimeType.DAY,
    // Точный рейтинг интервала по шкале 0..5.
    val rating: Double = activityLevel / 2.0
)