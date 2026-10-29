package com.fishingpoints.domain.recommendation.bait

import com.fishingpoints.data.models.FishingWeather
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaitRecommendationService @Inject constructor() {
    companion object {
        private const val COLD_WATER_THRESHOLD = 5.0
        private const val WARM_WATER_THRESHOLD = 15.0
        private const val HIGH_CLOUDINESS = 70
        private const val LOW_CLOUDINESS = 30
    }

    fun getBaitRecommendations(weather: FishingWeather): List<String> {

        val recommendations = mutableListOf<String>()

        // Температура воды
        addTemperatureBasedRecommendations(weather.temperature, recommendations)

        // Клёв
        addBiteRatingRecommendations(weather.biteRating.toInt(), recommendations)

        // Облачность
        addCloudinessRecommendations(weather.cloudiness, recommendations)

        return recommendations
    }

    private fun addTemperatureBasedRecommendations(temperature: Double?, recommendations: MutableList<String>) {
        if (temperature != null) {
            when {
                temperature < COLD_WATER_THRESHOLD -> {
                    recommendations.add("🐛 Мотыль - лучшая наживка в холодной воде")
                    recommendations.add("🪱 Черви - медленно двигаются, привлекают рыбу")
                }
                temperature in COLD_WATER_THRESHOLD..WARM_WATER_THRESHOLD -> {
                    recommendations.add("🪱 Навозный червь - универсальная наживка")
                    recommendations.add("🍞 Хлеб - хорошо работает в мутной воде")
                }
                temperature > WARM_WATER_THRESHOLD -> {
                    recommendations.add("🌽 Кукуруза - привлекает карпа и карася летом")
                    recommendations.add("🍦 Тесто с ароматизаторами - отлично работает в теплую погоду")
                    recommendations.add("🐟 Живец - для ловли щуки и судака")
                }
            }
        }
    }

    private fun addBiteRatingRecommendations(biteRating: Int, recommendations: MutableList<String>) {
        when (biteRating) {
            5, 4 -> recommendations.add("🎯 При активном клеве - наживка крупнее, можно комбинировать")
            3 -> recommendations.add("📊 При среднем клеве - стандартная наживка")
            2, 1 -> recommendations.add("👌 При пассивном клеве - наживка мелкая, малозаметная")
        }
    }

    private fun addCloudinessRecommendations(cloudiness: Int, recommendations: MutableList<String>) {
        when {
            cloudiness > HIGH_CLOUDINESS -> {
                recommendations.add("☁️ В пасмурную погоду - яркие искусственные приманки")
            }
            cloudiness < LOW_CLOUDINESS -> {
                recommendations.add("☀️ В солнечную погоду - натуральные цвета приманок")
            }

        }
    }
}