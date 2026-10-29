package com.fishingpoints.data.local.enums

enum class Season(
    val title: String,
    val icon: String
) {
    SPRING("Весна", "🌸"),
    SUMMER("Лето", "☀️"),
    AUTUMN("Осень", "🍂"),
    WINTER("Зима", "❄️"),
    ALL_YEAR("Круглый год", "📅");
    val displayName: String
        get() = "$icon $title"
}