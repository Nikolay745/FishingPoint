package com.fishingpoints.domain.service

import jakarta.inject.Inject

class WindAnalyzer @Inject constructor()  {
    fun analyze(
        windSpeed: Double,
        windDirection: Int
    ): String {

        val direction = getWindDirection(windDirection)

        val windDescription = when {
            windSpeed < 2 -> "Штиль или почти полный штиль"
            windSpeed < 4 -> "Слабый ветер"
            windSpeed < 7 -> "Умеренный ветер"
            windSpeed < 10 -> "Сильный ветер"
            else -> "Очень сильный ветер"
        }

        val fishingAdvice = when {
            windSpeed >= 10 ->
                "Рыбалка может быть затруднена из-за сильного ветра. Выбирайте защищённые участки водоёма."

            windSpeed >= 7 ->
                "Учитывайте волнение воды и выбирайте более защищённые места."

            windSpeed < 2 ->
                "При отсутствии ветра рыба может быть осторожнее. Попробуйте использовать более тонкие снасти."

            else ->
                "Умеренное движение воды может быть благоприятным для рыбалки."
        }

        val directionAdvice = when (direction) {
            "С", "СВ" ->
                "Северный ветер: учитывайте возможное похолодание воды."

            "Ю", "ЮЗ", "ЮВ" ->
                "Южный ветер: следите за изменением температуры воды."

            "В", "СЗ" ->
                "Обратите внимание на изменение погодных условий."

            "З" ->
                "Западный ветер: оцените его сочетание с изменением давления."

            else ->
                "Учитывайте местные особенности водоёма."
        }

        return "$windDescription ($direction, ${windDirection}°). " +
                "$fishingAdvice $directionAdvice"
    }

    private fun getWindDirection(degrees: Int): String {
        val normalized = ((degrees % 360) + 360) % 360

        return when (normalized) {
            in 338..359, in 0..22 -> "С"
            in 23..67 -> "СВ"
            in 68..112 -> "В"
            in 113..157 -> "ЮВ"
            in 158..202 -> "Ю"
            in 203..247 -> "ЮЗ"
            in 248..292 -> "З"
            in 293..337 -> "СЗ"
            else -> "—"
        }
    }
}
