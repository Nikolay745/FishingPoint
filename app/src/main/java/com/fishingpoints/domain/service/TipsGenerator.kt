package com.fishingpoints.domain.service

import com.fishingpoints.data.models.FishingWeather
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipsGenerator @Inject constructor() {

    fun getTimeOfDayRecommendation(hour: Int): String {
        return when (hour) {
            in 4..8 -> "🌅 Раннее утро (4-8) - Лучшее время для ловли мирной рыбы. Карась, плотва, лещ активно кормятся у поверхности."
            in 8..11 -> "☀️ Утро (8-11) - Хорошее время для хищника. Щука и окунь выходят на охоту."
            in 11..14 -> "🌞 Полдень (11-14) - Рыба уходит в глубину. Используйте донные снасти или ищите тенистые места."
            in 14..18 -> "🌤️ Послеобеденное время - Активность восстанавливается. Хорошо работает фидер и донка."
            in 18..22 -> "🌙 Вечер (18-22) - Начинается вечерний жор хищника. Ловите на спиннинг у зарослей камыша."
            else -> "🌃 Ночь (22-4) - Ловля сомов, налимов, ночного карася. Используйте светящиеся поплавки и ароматизированные наживки."
        }
    }

    fun getGroundbaitRecommendations(weather: FishingWeather): String {
        val temperature = weather.temperature ?: return "🌡️ Нет данных о температуре"

        return when {
            weather.temperature < 5 -> {
                "❄️ Прикормка: Животного происхождения (рубленый червь, мотыль). Не ароматизировать - холодная вода замедляет распространение запаха."
            }
            weather.temperature in 5.0..15.0 -> "🌊 Прикормка: Смесь панировочных сухарей с добавлением конопли или подсолнечного жмыха. 300-500г на рыбалку."
            weather.temperature > 15 -> "🔥 Прикормка: Сладкая (мед, патока, ваниль). Замешивайте с водой непосредственно перед рыбалкой. 500-1000г."
            else -> "📦 Прикормка: Стартовая - 500г, далее по 100г каждый час. Добавьте грунт для тяжести в прикормку."
        }
    }

    fun getAttractantRecommendations(weather: FishingWeather): String {
        val temperature = weather.temperature ?: return "🌡️ Нет данных о температуре"

        return when {
            weather.temperature < 5 -> {
                "🦐 Аромка: Креветка, краб (водорастворимые аромки)"
            }
            weather.temperature in 5.0..15.0 -> "🧄 Аромка: Чеснок, конопля (универсальные ароматы)"
            weather.temperature > 15 -> "🍓 Аромка: Фруктовые и ягодные (клубника, персик, карамель)"
            else -> "🎨 Экспериментируйте: В жаркую погоду - кислые ароматы (слива, лимон), в холодную - сладкие"
        }
    }


}