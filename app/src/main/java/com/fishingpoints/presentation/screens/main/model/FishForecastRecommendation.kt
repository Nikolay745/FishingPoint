package com.fishingpoints.presentation.screens.main.model

import com.fishingpoints.data.local.enums.TimeOfDay

data class FishForecastRecommendation(
    val fishId: Long,
    val fishName: String,
    val rating: Double,
    val activityLevel: Int,
    val reasons: List<String>,
    val bestTimeOfDay: List<TimeOfDay>,
    val tipsBaits: String?,
    val tackles: String?,
    val baitForFish: String?,
    val bestTimes: List<FishTimeForecast> = emptyList()
)