package com.fishingpoints.domain.lunar

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.cos

@Singleton
class LunarCalendar @Inject constructor() {

    fun getLunarInfo(date: LocalDate): LunarInfo {
        val (phase, illumination) = calculateMoonPhase(date)
        val biteRating = calculateBiteRatingByMoon(phase)
        val recommendation = getMoonRecommendation(phase, biteRating)
        val coefficient = getLunarCoefficient(phase)

        return LunarInfo(
            phase = phase,
            phaseName = getPhaseName(phase),
            illumination = illumination,
            biteRating = biteRating,
            recommendation = recommendation,
            coefficient = coefficient
        )
    }

    private fun calculateMoonPhase(date: LocalDate): Pair<MoonPhase, Double> {
        val epoch = LocalDate.of(2000, 1, 6)
        val diffInDays = ChronoUnit.DAYS.between(epoch, date).toDouble()
        val lunarCycle = 29.53058867
        val phaseIndex = (diffInDays % lunarCycle) / lunarCycle

        val illumination = (1 - cos(phaseIndex * 2 * PI)) / 2

        val phase = when {
            phaseIndex < 0.03 -> MoonPhase.NEW_MOON
            phaseIndex < 0.22 -> MoonPhase.WAXING_CRESCENT
            phaseIndex < 0.28 -> MoonPhase.FIRST_QUARTER
            phaseIndex < 0.47 -> MoonPhase.WAXING_GIBBOUS
            phaseIndex < 0.53 -> MoonPhase.FULL_MOON
            phaseIndex < 0.72 -> MoonPhase.WANING_GIBBOUS
            phaseIndex < 0.78 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }

        return Pair(phase, illumination * 100)
    }


    private fun calculateBiteRatingByMoon(phase: MoonPhase): Int {
        return when (phase) {
            // Первая и последняя четверть — оптимально (освещенность 50%)
            MoonPhase.FIRST_QUARTER, MoonPhase.LAST_QUARTER -> 5

            // Растущая/убывающая (25-75%) — хорошо
            MoonPhase.WAXING_GIBBOUS, MoonPhase.WANING_GIBBOUS -> 4

            // Молодая/старая (до 25%) — средне
            MoonPhase.WAXING_CRESCENT, MoonPhase.WANING_CRESCENT -> 3

            // Полнолуние/новолуние — плохо (стресс для рыбы)
            MoonPhase.NEW_MOON, MoonPhase.FULL_MOON -> 2
        }
    }

    // Дополнительно: коэффициент для расчета biteRating
    fun getLunarCoefficient(phase: MoonPhase): Double {
        return when (phase) {
            MoonPhase.FIRST_QUARTER, MoonPhase.LAST_QUARTER -> 1.3
            MoonPhase.WAXING_GIBBOUS, MoonPhase.WANING_GIBBOUS -> 1.15
            MoonPhase.WAXING_CRESCENT, MoonPhase.WANING_CRESCENT -> 1.0
            MoonPhase.NEW_MOON, MoonPhase.FULL_MOON -> 0.7
        }
    }

     private fun getMoonRecommendation(phase: MoonPhase, biteRating: Int): String {
        return when (phase) {
            MoonPhase.NEW_MOON -> "🌑 Новолуние: клёв слабый (${biteRating}/5). Рыба пассивна."
            MoonPhase.FULL_MOON -> "🌕 Полнолуние: клёв ниже среднего (${biteRating}/5). Лучше ночью."
            MoonPhase.FIRST_QUARTER -> "🌓 Первая четверть: отличный клёв (${biteRating}/5)! Утро — лучшее время."
            MoonPhase.LAST_QUARTER -> "🌗 Последняя четверть: хороший клёв (${biteRating}/5). Ловите после обеда."
            MoonPhase.WAXING_GIBBOUS -> "🌔 Растущая луна: хороший клёв (${biteRating}/5). Хищник активен."
            MoonPhase.WANING_GIBBOUS -> "🌖 Убывающая луна: стабильный клёв (${biteRating}/5). Мирная рыба кормится."
            MoonPhase.WAXING_CRESCENT -> "🌒 Молодая луна: средний клёв (${biteRating}/5). Ловите на глубине."
            MoonPhase.WANING_CRESCENT -> "🌘 Старая луна: средний клёв (${biteRating}/5). Рыба осторожна."
        }
    }

    private fun getPhaseName(phase: MoonPhase): String {
        return when (phase) {
            MoonPhase.NEW_MOON -> "🌑 Новолуние"
            MoonPhase.WAXING_CRESCENT -> "🌒 Молодая луна"
            MoonPhase.FIRST_QUARTER -> "🌓 Первая четверть"
            MoonPhase.WAXING_GIBBOUS -> "🌔 Растущая луна"
            MoonPhase.FULL_MOON -> "🌕 Полнолуние"
            MoonPhase.WANING_GIBBOUS -> "🌖 Убывающая луна"
            MoonPhase.LAST_QUARTER -> "🌗 Последняя четверть"
            MoonPhase.WANING_CRESCENT -> "🌘 Старая луна"
        }
    }
}


