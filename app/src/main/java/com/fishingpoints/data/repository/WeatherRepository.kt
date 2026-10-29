package com.fishingpoints.data.repository

import com.fishingpoints.core.network.WeatherApiService
import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.WeatherResponse
import com.fishingpoints.domain.UserSettingsDataSource
import com.fishingpoints.presentation.screens.settings.MeasurementSystem
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import com.fishingpoints.presentation.screens.settings.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApiService,
    private val settings: UserSettingsDataSource
) {
    private var cachedCurrentWeather: WeatherResponse? = null
    private var cachedForecast: ForecastResponse? = null

    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherResponse {
        val response = api.getCurrentWeather(lat, lon)

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException(
                "Ошибка погоды: HTTP ${response.code()} ${response.message()}"
            )
        }

        return response.body()!!.also {
            cachedCurrentWeather = it
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): ForecastResponse {
        val response = api.getForecast(lat, lon)

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Failed to load forecast")
        }

        return response.body()!!.also {
            cachedForecast = it
        }
    }

    // Получение текущего режима
    fun getCachedCurrentWeather(): WeatherResponse? =
        cachedCurrentWeather

    fun getCachedForecast(): ForecastResponse? =
        cachedForecast

    // Температура воды
    fun isUsingRealWaterTemperature(): Boolean {
        return settings.isUsingRealWaterTemperature()
    }

    fun getRealWaterTemperature(): Double {
        return settings.getRealWaterTemperature()
    }

    fun setUseRealWaterTemperature(useReal: Boolean) {
        settings.setUseRealWaterTemperature(useReal)
    }

    fun saveRealWaterTemperature(temperature: Double) {
        settings.saveRealWaterTemperature(temperature)
    }

    // Единицы температуры
    fun getTemperatureUnit(): TemperatureUnit =
        settings.getTemperatureUnit()

    fun setTemperatureUnit(unit: TemperatureUnit) {
        settings.setTemperatureUnit(unit)
    }

    // Система измерения
    fun getMeasurementSystem(): MeasurementSystem =
        settings.getMeasurementSystem()

    fun setMeasurementSystem(system: MeasurementSystem) {
        settings.setMeasurementSystem(system)

    }

    // Уведомления
    fun areNotificationsEnabled(): Boolean =
        settings.areNotificationsEnabled()

    fun setNotificationsEnabled(enabled: Boolean) {
        settings.setNotificationsEnabled(enabled)
    }

    // Тема приложения
    fun getThemeMode(): ThemeMode =
        settings.getThemeMode()

    fun setThemeMode(mode: ThemeMode) {
        settings.setThemeMode(mode)
    }

}