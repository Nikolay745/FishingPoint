package com.fishingpoints.data.local.enums

// Активность клева
enum class FeedingActivity(
    val displayName: String,
    val icon: String
) { HIGH("Высокая", "🐡"),
    MEDIUM("Средняя", "🐠"),
    LOW("Низкая", "🐟"),
    NONE("Нет данных", "🐙"),
    VERY_HIGH("Очень высокая", "🐙");

    val displayText: String
        get() = "$icon $displayName"

}
