package com.fishingpoints.domain.recommendation.tackle

import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TackleRecommendationService @Inject constructor() {

    fun getTackleRecommendations(weather: FishingWeather): List<String> {
        val recommendations = mutableListOf<String>()

        // Рекомендации по удилищу
        when {
            weather.windSpeed > 8 -> recommendations.add("🎣 Удилище: Жесткое, с быстрым строем (для заброса против ветра)")
            weather.windSpeed > 5 -> recommendations.add("🎣 Удилище: Среднежесткое, длина 3-4 метра")
            else -> recommendations.add("🎣 Удилище: Чувствительное, для осторожной поклевки")
        }

        // Рекомендации по леске
        when {
            weather.temperature!! < 0 -> recommendations.add("🧵 Леска: Монофильная (не дубеет на морозе)")
            weather.temperature > 20 -> recommendations.add("🧵 Леска: Плетенка (меньше растяжения, лучше контроль)")
            else -> recommendations.add("🧵 Леска: 0.18-0.25 мм для универсальной ловли")
        }

        // Рекомендации по крючкам
        when (weather.biteRating) {
            5.0, 4.0 -> recommendations.add("🪝 Крючки: №6-10 (активный клев, рыба берет смело)")
            3.0 -> recommendations.add("🪝 Крючки: №12-16 (средняя активность)")
            else -> recommendations.add("🪝 Крючки: №16-20 (пассивный клев, нужен маленький крючок)")
        }

        // Давление
        when (weather.pressureTrend) {
            PressureTrend.FALLING -> recommendations.add("📈 Давление падает: Используйте активную проводку, рыба активна!")
            PressureTrend.RISING -> recommendations.add("📉 Давление растет: Ловите на дне, пассивная проводка")
            PressureTrend.STABLE -> recommendations.add("⚖️ Давление стабильно: Лучшее время для экспериментов со снастями")
            PressureTrend.NODATA -> recommendations.add("⚖️ Давление не определено")
        }

        return recommendations
    }
}