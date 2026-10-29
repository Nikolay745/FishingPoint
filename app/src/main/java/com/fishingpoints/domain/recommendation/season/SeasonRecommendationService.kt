package com.fishingpoints.domain.recommendation.season

import java.time.Month
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonRecommendationService @Inject constructor() {

    fun getSeasonRecommendation(month: Month): String {
        return when (month) {
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY ->
                "❄️ ЗИМА - Лучшая рыбалка со льда. Мормышка, блесна, балансир. Осторожно на льду! Толщина должна быть не менее 7см."
            Month.MARCH, Month.APRIL, Month.MAY ->
                "🌸 ВЕСНА - Рыба готовится к нересту. В марте-апреле рыба активно питается. Запрещена ловля в период нереста (апрель-май)!"
            Month.JUNE, Month.JULY, Month.AUGUST ->
                "☀️ ЛЕТО - Рыбалка на рассвете и закате. Днем рыба уходит в глубину. Используйте ароматизаторы и прикормку."
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER ->
                "🍂 ОСЕНЬ - Осенний жор! Хищник активен весь день. Отличное время для спиннинга и кружков."
        }
    }

    // Для обратной совместимости
    @Deprecated("Use Month version", ReplaceWith("getSeasonRecommendation(Month.of(month + 1))"))
    fun getSeasonRecommendation(month: Int): String {
        require(month in 0..11) { "Month must be 0-11" }
        return getSeasonRecommendation(Month.of(month + 1))
    }
}