package com.fishingpoints.presentation.screens.settings

import androidx.lifecycle.ViewModel
import com.fishingpoints.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _temperatureUnit = MutableStateFlow(
        weatherRepository.getTemperatureUnit()
    )
    val temperatureUnit: StateFlow<TemperatureUnit> =
        _temperatureUnit.asStateFlow()

    private val _measurementSystem = MutableStateFlow(
        weatherRepository.getMeasurementSystem()
    )
    val measurementSystem: StateFlow<MeasurementSystem> =
        _measurementSystem.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        weatherRepository.areNotificationsEnabled()
    )
    val notificationsEnabled: StateFlow<Boolean> =
        _notificationsEnabled.asStateFlow()

    fun setTemperatureUnit(unit: TemperatureUnit) {
        weatherRepository.setTemperatureUnit(unit)
        _temperatureUnit.value = unit
    }

    fun setMeasurementSystem(system: MeasurementSystem) {
        weatherRepository.setMeasurementSystem(system)
        _measurementSystem.value = system
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        weatherRepository.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }

}
