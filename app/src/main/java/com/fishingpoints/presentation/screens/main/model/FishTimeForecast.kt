package com.fishingpoints.presentation.screens.main.model

import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import java.time.LocalTime

data class FishTimeForecast(
    val start: LocalTime,
    val end: LocalTime,
    val rating: Double,
    val activityLevel: Int,
    val description: String,
    val reasons: List<String>,

    // Индивидуальная активность рыбы
    val feedingActivity: FeedingActivity = FeedingActivity.NONE,
    val activityDescription: String? = null,
    val ruleConfidence: RuleConfidence? = null
)
