package com.fishingpoints.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.fishingpoints.domain.UserSettingsDataSource
import com.fishingpoints.presentation.screens.settings.MeasurementSystem
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import com.fishingpoints.presentation.screens.settings.ThemeMode
import javax.inject.Inject

class SharedPrefsUserSettingsDataSource @Inject constructor(
    private val prefs: SharedPreferences
) : UserSettingsDataSource {

    companion object {
        private const val KEY_USE_REAL_WATER_TEMP = "use_real_water_temp"
        private const val KEY_REAL_WATER_TEMP = "real_water_temp"

        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_MEASUREMENT_SYSTEM = "measurement_system"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        private const val KEY_THEME_MODE = "theme_mode"
    }

    // ---------------------------------------------------------
    // Температура воды
    // ---------------------------------------------------------
    override fun isUsingRealWaterTemperature(): Boolean {
        return prefs.getBoolean(KEY_USE_REAL_WATER_TEMP, false)
    }

    override fun getRealWaterTemperature(): Double {
        // Чтение строки сохраняет точность Double без потерь
        return prefs.getString(KEY_REAL_WATER_TEMP, "12.0")?.toDoubleOrNull() ?: 12.0
    }

    override fun setUseRealWaterTemperature(useReal: Boolean) {
        prefs.edit { putBoolean(KEY_USE_REAL_WATER_TEMP, useReal) }
    }

    override fun saveRealWaterTemperature(temperature: Double) {
        // Сохранение строки вместо float предотвращает потерю точности
        prefs.edit { putString(KEY_REAL_WATER_TEMP, temperature.toString()) }
    }

    // ---------------------------------------------------------
    // Единицы температуры
    // ---------------------------------------------------------

    override fun getTemperatureUnit(): TemperatureUnit {
        val value = prefs.getString(
            KEY_TEMPERATURE_UNIT,
            TemperatureUnit.CELSIUS.name
        )

        return runCatching {
            TemperatureUnit.valueOf(value ?: TemperatureUnit.CELSIUS.name)
        }.getOrDefault(TemperatureUnit.CELSIUS)
    }

    override fun setTemperatureUnit(unit: TemperatureUnit) {
        prefs.edit {
            putString(
                KEY_TEMPERATURE_UNIT,
                unit.name
            )
        }
    }

    // ---------------------------------------------------------
    // Система измерения
    // ---------------------------------------------------------

    override fun getMeasurementSystem(): MeasurementSystem {
        val value = prefs.getString(
            KEY_MEASUREMENT_SYSTEM,
            MeasurementSystem.METRIC.name
        )

        return runCatching {
            MeasurementSystem.valueOf(
                value ?: MeasurementSystem.METRIC.name
            )
        }.getOrDefault(MeasurementSystem.METRIC)
    }

    override fun setMeasurementSystem(system: MeasurementSystem) {
        prefs.edit {
            putString(
                KEY_MEASUREMENT_SYSTEM,
                system.name
            )
        }
    }

    // ---------------------------------------------------------
    // Уведомления
    // ---------------------------------------------------------

    override fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(
            KEY_NOTIFICATIONS_ENABLED,
            true
        )
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                enabled
            )
        }
    }

    // ---------------------------------------------------------
// Тема приложения
// ---------------------------------------------------------

    override fun getThemeMode(): ThemeMode {
        val value = prefs.getString(
            KEY_THEME_MODE,
            ThemeMode.DARK.name
        )

        return runCatching {
            ThemeMode.valueOf(value ?: ThemeMode.DARK.name)
        }.getOrDefault(ThemeMode.DARK)
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit {
            putString(KEY_THEME_MODE, mode.name)
        }
    }


}