package com.fishingpoints.presentation.screens.fish.components

import com.fishingpoints.data.local.enums.FeedingActivity

fun getFeedingActivityIcon(activity: FeedingActivity?): String {
    return when (activity) {
        FeedingActivity.LOW -> "🐟 Низкая"
        FeedingActivity.MEDIUM -> "🐠 Средняя"
        FeedingActivity.HIGH -> "🐡 Высокая"
        FeedingActivity.VERY_HIGH -> "🐙 Очень высокая"
        FeedingActivity.NONE -> "🐙 Нет данных"
        null -> "🐙 Нет данных"
    }
}