package com.fishingpoints.presentation.screens.main.model

import java.time.LocalTime

data class FishTimeForecast(
    val start: LocalTime,
    val end: LocalTime,
    val rating: Double,
    val activityLevel: Int,
    val description: String,
    val reasons: List<String>
)