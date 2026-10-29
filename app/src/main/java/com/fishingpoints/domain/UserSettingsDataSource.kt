package com.fishingpoints.domain

import com.fishingpoints.presentation.screens.settings.MeasurementSystem
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import com.fishingpoints.presentation.screens.settings.ThemeMode

interface UserSettingsDataSource {

    // Температура воды
    fun isUsingRealWaterTemperature(): Boolean
    fun getRealWaterTemperature(): Double
    fun setUseRealWaterTemperature(useReal: Boolean)
    fun saveRealWaterTemperature(temperature: Double)

    // Единицы температуры
    fun getTemperatureUnit(): TemperatureUnit
    fun setTemperatureUnit(unit: TemperatureUnit)

    // Система измерения
    fun getMeasurementSystem(): MeasurementSystem
    fun setMeasurementSystem(system: MeasurementSystem)

    // Уведомления
    fun areNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)

    // Тема приложения
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)

}

