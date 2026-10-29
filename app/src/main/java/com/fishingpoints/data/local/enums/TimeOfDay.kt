package com.fishingpoints.data.local.enums

enum class TimeOfDay(
    val title: String,
    val icon: String
) {
    NIGHT("Ночь", "🌙"),
    DAWN("Рассвет", "🌅"),
    MORNING("Утро", "🌄"),
    DAY("День", "☀️"),
    AFTERNOON("После полудня", "🌤️"),
    EVENING("Вечер", "🌇"),
    DUSK("Сумерки", "🌆"),
    ANY("Не указано", "⏰");

    val displayName: String
        get() = "$icon $title"
}