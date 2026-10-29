package com.fishingpoints.domain.recommendation.tips

import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.domain.lunar.LunarInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PersonalizedTipService @Inject constructor(
    private val random: Random
) {

    fun getPersonalizedTip(
        weather: FishingWeather,
        lunarInfo: LunarInfo?
    ): String {

        val tips = listOf(
            "💡 Совет: Всегда проверяйте снасти перед рыбалкой. Слабая леска может подвести при поклевке трофея!",
            "💡 Совет: Прикармливайте место за 30-40 минут до начала рыбалки. Рыба подойдет не сразу.",
            "💡 Совет: В ветреную погоду выбирайте место в укрытии - за мысом, островом или в зарослях камыша.",
            "💡 Совет: Меняйте глубину ловли каждые 20 минут, если поклевок нет. Рыба может стоять на разных горизонтах.",
            "💡 Совет: Записывайте успешные места и условия в дневник. Со временем вы увидите закономерности!",
            "💡 Совет: Самые большие экземпляры часто стоят отдельно от стаи. Ищите их у коряг и бровок.",
            "💡 Совет: При ловле на фидер не перекармливайте. 3-4 стартовых заброса достаточно."
        )

        val specificTips = buildSet {
            // Совет по погоде
            if (weather.biteRating in 4.0..5.0) {
                add("🎉 Сегодня отличный клев! Не упустите момент - используйте активные методы ловли!")
            }

            // Совет по лунному календарю
            lunarInfo?.let { moon ->
                if (moon.biteRating in 4..5) {
                    add("🌙 ${moon.recommendation}")
                }
            }

            // Совет по изменению давления
            when (weather.pressureTrend) {
                PressureTrend.FALLING -> add("⚡ Давление падает - рыба клюет активно! Быстрее на рыбалку!")
                PressureTrend.RISING -> add("📈 Давление растет - клев может быть нестабильным. Будьте терпеливы!")
                PressureTrend.STABLE -> add("🎯 Давление стабильно - отличные условия для прогноза клева!")
                PressureTrend.NODATA -> { /* Нет данных о давлении - не добавляем совет */ }

            }
        }.toList()

        // Если есть специфические советы - берем случайный из них, иначе - общий совет
        return specificTips.randomOrNull(random) ?: tips.random(random)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RandomModule {
    @Provides
    @Singleton
    fun provideRandom(): Random = Random.Default
}


