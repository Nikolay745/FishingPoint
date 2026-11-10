package com.fishingpoints.domain.recommendation.tips

import com.fishingpoints.core.network.FishingForecast
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.domain.lunar.LunarInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalizedTipService @Inject constructor() {

    private data class TipCandidate(
        val priority: Int,
        val message: String
    )

    fun getPersonalizedTip(
        weather: FishingWeather,
        lunarInfo: LunarInfo?,
        fishingForecast: FishingForecast? = null
    ): String {

        val candidates = mutableListOf<TipCandidate>()

        // ==========================================
        // 1. РЕЙТИНГ КЛЁВА
        // ==========================================

        when {
            weather.biteRating < 2.0 -> {
                candidates.add(
                    TipCandidate(
                        100,
                        "🐟 Сегодня прогноз клёва неблагоприятный. " +
                                "Не задерживайтесь на одной точке без поклёвок. " +
                                "Попробуйте уменьшить размер наживки, " +
                                "изменить глубину и проверить другие участки водоёма."
                    )
                )
            }

            weather.biteRating < 3.0 -> {
                candidates.add(
                    TipCandidate(
                        85,
                        "🎣 Активность рыбы может быть невысокой. " +
                                "Используйте умеренное количество прикормки, " +
                                "экспериментируйте с наживкой и глубиной ловли."
                    )
                )
            }

            weather.biteRating < 4.0 -> {
                candidates.add(
                    TipCandidate(
                        65,
                        "🎯 Сегодня ожидаются умеренные условия для рыбалки. " +
                                "Начните с проверенной наживки и наблюдайте за реакцией рыбы."
                    )
                )
            }

            else -> {
                candidates.add(
                    TipCandidate(
                        45,
                        "🎉 Прогноз клёва благоприятный! " +
                                "Подготовьте проверенные снасти и используйте наиболее перспективные часы ловли."
                    )
                )
            }
        }

        // ==========================================
        // 2. СИЛА ВЕТРА
        // ==========================================

        when {
            weather.windSpeed >= 10.0 -> {
                candidates.add(
                    TipCandidate(
                        95,
                        "🌬 Сегодня сильный ветер. " +
                                "Ищите защищённые участки водоёма, " +
                                "учитывайте направление ветра и выбирайте устойчивую оснастку."
                    )
                )
            }

            weather.windSpeed >= 6.0 -> {
                candidates.add(
                    TipCandidate(
                        75,
                        "🌬 Ветер достаточно заметный. " +
                                "Обратите внимание на подветренные участки " +
                                "и контролируйте положение оснастки."
                    )
                )
            }

            weather.windSpeed <= 2.0 -> {
                candidates.add(
                    TipCandidate(
                        40,
                        "🍃 Ветер слабый. " +
                                "Старайтесь соблюдать тишину у воды " +
                                "и аккуратно подавать наживку."
                    )
                )
            }
        }

        // ==========================================
        // 3. ТЕМПЕРАТУРА ВОДЫ
        // ==========================================

        weather.waterTemperature?.let { waterTemp ->

            when {
                waterTemp <= 5.0 -> {
                    candidates.add(
                        TipCandidate(
                            90,
                            "❄️ Вода холодная. " +
                                    "Попробуйте уменьшить количество прикормки " +
                                    "и использовать более деликатную подачу наживки."
                        )
                    )
                }

                waterTemp >= 25.0 -> {
                    candidates.add(
                        TipCandidate(
                            90,
                            "☀️ Вода сильно прогрета. " +
                                    "Обратите внимание на ранние утренние и вечерние часы, " +
                                    "когда условия могут быть комфортнее для рыбы."
                        )
                    )
                }
            }
        }

        // ==========================================
        // 4. ТЕМПЕРАТУРА ВОЗДУХА
        // ==========================================

        weather.temperature?.let { temperature ->

            when {
                temperature <= -5.0 -> {
                    candidates.add(
                        TipCandidate(
                            70,
                            "🥶 На улице мороз. " +
                                    "Проверьте состояние лески и колец удилища, " +
                                    "а также учитывайте безопасность на водоёме."
                        )
                    )
                }

                temperature >= 30.0 -> {
                    candidates.add(
                        TipCandidate(
                            80,
                            "🌡 Сегодня жаркая погода. " +
                                    "Рассмотрите раннюю утреннюю или вечернюю рыбалку " +
                                    "и избегайте длительного пребывания на солнце."
                        )
                    )
                }
            }
        }

        // ==========================================
        // 5. ОБЛАЧНОСТЬ
        // ==========================================

        when {
            weather.cloudiness >= 85 -> {
                candidates.add(
                    TipCandidate(
                        35,
                        "☁️ Небо преимущественно затянуто облаками. " +
                                "Ориентируйтесь на фактическую активность рыбы " +
                                "и проверяйте разные горизонты ловли."
                    )
                )
            }

            weather.cloudiness <= 15 -> {
                candidates.add(
                    TipCandidate(
                        35,
                        "☀️ Сегодня малооблачная погода. " +
                                "При ярком солнце попробуйте искать рыбу " +
                                "в затенённых участках водоёма."
                    )
                )
            }
        }

        // ==========================================
        // 6. АТМОСФЕРНОЕ ДАВЛЕНИЕ
        // ==========================================

        when (weather.pressureTrend) {

            PressureTrend.FALLING -> {
                candidates.add(
                    TipCandidate(
                        55,
                        "📉 Атмосферное давление снижается. " +
                                "Наблюдайте за изменением активности рыбы " +
                                "и следите за погодными условиями."
                    )
                )
            }

            PressureTrend.RISING -> {
                candidates.add(
                    TipCandidate(
                        55,
                        "📈 Атмосферное давление повышается. " +
                                "Попробуйте проверить разные горизонты ловли " +
                                "и не торопитесь менять рабочую наживку."
                    )
                )
            }

            PressureTrend.STABLE -> Unit

            PressureTrend.NODATA -> Unit
        }

        // ==========================================
        // 7. ОСАДКИ
        // ==========================================

        val precipitation = weather.precipitation.lowercase()

        when {
            "гроза" in precipitation -> {
                candidates.add(
                    TipCandidate(
                        110,
                        "⛈ Сегодня возможна гроза. " +
                                "Не выходите на открытую воду и прекратите рыбалку " +
                                "при приближении грозы. Безопасность важнее улова."
                    )
                )
            }

            "дождь" in precipitation -> {
                candidates.add(
                    TipCandidate(
                        70,
                        "🌧 Ожидаются осадки. " +
                                "Возьмите непромокаемую одежду и следите за изменением " +
                                "уровня и состояния воды."
                    )
                )
            }

            "снег" in precipitation -> {
                candidates.add(
                    TipCandidate(
                        65,
                        "❄️ Возможен снег. " +
                                "Подготовьте снасти к холодным условиям " +
                                "и учитывайте ухудшение видимости."
                    )
                )
            }
        }

        // ==========================================
        // 8. ЛУННЫЙ ПРОГНОЗ
        // ==========================================

        lunarInfo?.let { moon ->

            if (moon.biteRating <= 2) {
                candidates.add(
                    TipCandidate(
                        30,
                        "🌙 Лунный прогноз неблагоприятный. " +
                                "Не ориентируйтесь только на календарь: " +
                                "наблюдайте за реальными поклёвками."
                    )
                )
            }
        }

        // ==========================================
        // 9. ЛУЧШЕЕ ВРЕМЯ ЛОВЛИ
        // ==========================================

        fishingForecast?.bestTimes
            ?.maxByOrNull { it.rating }
            ?.let { bestTime ->

                if (bestTime.rating >= 4.0) {
                    candidates.add(
                        TipCandidate(
                            88,
                            "⏰ Самое перспективное время сегодня — " +
                                    "${bestTime.start}–${bestTime.end}. " +
                                    "Постарайтесь подготовить снасти заранее " +
                                    "и использовать этот период для основной ловли."
                        )
                    )
                }
            }

        // ==========================================
        // 10. ВЫБОР ОДНОГО СОВЕТА
        // ==========================================

        return candidates
            .maxByOrNull { it.priority }
            ?.message
            ?: "🎣 Сегодня наблюдайте за поведением рыбы, " +
            "проверяйте перспективные участки водоёма " +
            "и подбирайте тактику по фактическим поклёвкам."
    }
}



