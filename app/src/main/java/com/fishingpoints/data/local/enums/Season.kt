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
    companion object {
        fun current(): Season {
            return when (java.time.LocalDate.now().monthValue) {
                in 3..5 -> SPRING
                in 6..8 -> SUMMER
                in 9..11 -> AUTUMN
                else -> WINTER
            }
        }
    }

}