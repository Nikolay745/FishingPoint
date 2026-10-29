package com.fishingpoints.presentation.screens.main

import com.fishingpoints.core.network.FishingForecast
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.domain.lunar.LunarInfo
import com.fishingpoints.presentation.screens.main.model.BestFishingTime
import com.fishingpoints.presentation.screens.main.model.FishForecastRecommendation

sealed class AppState {
    data object Idle : AppState()
    data object LoadingLocation : AppState()
    data object LoadingWeather : AppState()
    data object Ready : AppState()
    data class Error(val message: String) : AppState()
}


data class MainUiState(

    // =========================================
    // Состояние приложения
    // =========================================

    val appState: AppState = AppState.Idle,

    // =========================================
    // Местоположение
    // =========================================

    val location: LocationEntity? = null,

    val locationName: String? = null,

    val regionName: String? = null,

    val isUsingDefaultLocation: Boolean = false,

    // =========================================
    // Погодные данные
    // =========================================

    val weather: FishingWeather? = null,

    // =========================================
    // Прогноз рыбалки
    // =========================================

    val fishingForecast: FishingForecast? = null,

    val isFishingForecastLoading: Boolean = false,

    val fishingForecastError: String? = null,

    // =========================================
    // Лунная информация
    // =========================================

    val lunarInfo: LunarInfo? = null,

    // =========================================
    // Дневные рекомендации
    // =========================================

    val dailyTips: DailyTips = DailyTips(),

    // =========================================
    // Временные интервалы для рыбалки
    // =========================================

    val allFishingTimes: List<BestFishingTime> = emptyList(),

    // =========================================
    // Рекомендованные рыбы
    // =========================================

    val recommendedFish: List<Fish> = emptyList(),

    // =========================================
    // Индивидуальные прогнозы рыб
    // =========================================

    val fishForecasts: List<FishForecastRecommendation> = emptyList(),

    /**
     * true — индивидуальные прогнозы рыб рассчитываются.
     * false — расчёт завершён или ещё не начат.
     */
    val isFishForecastsLoading: Boolean = false,
        // отделить отказ от разрешения на геолокацию от запрета на использование основного экрана
    val canUseDefaultLocation: Boolean = false
)
