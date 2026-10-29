package com.fishingpoints.presentation.formatting

import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

// Вспомогательные функции
fun formatTimeRange(
    start: LocalTime,
    end: LocalTime
): String {
    return "%02d:%02d - %02d:%02d".format(
        start.hour,
        start.minute,
        end.hour,
        end.minute
    )
}

fun formatHour(hour: Int): String {
    return hour.toString().padStart(2, '0') + ":00"
}

fun formatWeight(
    grams: Long
): String {
    return when {
        grams < 1000 ->
            "$grams г"

        grams < 10_000 -> {
            val kg = grams / 1000.0
            String.format(
                Locale.getDefault(),
                "%.1f кг",
                kg
            )
        }
        else -> {
            val kg = grams / 1000
            "$kg кг"
        }
    }
}

fun formatTemperature(
    temperatureCelsius: Double?,
    unit: TemperatureUnit
): String {
    if (temperatureCelsius == null) {
        return "Нет данных"
    }

    val temperature = when (unit) {
        TemperatureUnit.CELSIUS -> temperatureCelsius
        TemperatureUnit.FAHRENHEIT ->
            temperatureCelsius * 9.0 / 5.0 + 32.0
    }

    return String.format(
        Locale.US,
        "%.1f°${if (unit == TemperatureUnit.CELSIUS) "C" else "F"}",
        temperature
    )
}

fun formatFishingDate(date: Date): String {
    return SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault()
    ).format(date)
}

fun formatFishingTime(date: Date?): String {
    if (date == null) return "—"

    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(date)
}

fun formatFishingDateTime(date: Date?): String {
    if (date == null) return "—"

    return SimpleDateFormat(
        "dd.MM.yyyy HH:mm",
        Locale.getDefault()
    ).format(date)
}