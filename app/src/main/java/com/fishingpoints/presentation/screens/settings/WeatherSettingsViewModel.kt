package com.fishingpoints.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherSettingsViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _useRealWaterTemp = MutableStateFlow(false)
    val useRealWaterTemp: StateFlow<Boolean> = _useRealWaterTemp.asStateFlow()

    private val _realWaterTemp = MutableStateFlow(12.0)
    val realWaterTemp: StateFlow<Double> = _realWaterTemp.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        loadSettings()
    }


    private fun loadSettings() {
        viewModelScope.launch {
            _useRealWaterTemp.value = weatherRepository.isUsingRealWaterTemperature()
            // получение сохраненной температуры
            _realWaterTemp.value = weatherRepository.getRealWaterTemperature()
        }
    }

    fun setUseRealWaterTemp(useReal: Boolean) {
        _useRealWaterTemp.value = useReal

        viewModelScope.launch {
            try {
                weatherRepository.setUseRealWaterTemperature(useReal)

                _snackbarMessage.emit(
                    if (useReal) {
                        "🌡️ Включена реальная температура воды"
                    } else {
                        "📊 Включена расчетная температура воды"
                    }
                )
            } catch (_: Exception) {
                _snackbarMessage.emit(
                    "Ошибка сохранения настройки"
                )
            }
        }
    }

    fun saveRealWaterTemp(temp: Double) {

        if (temp !in -2.0..35.0) {
            return
        }

        viewModelScope.launch {
            try {
                weatherRepository.saveRealWaterTemperature(temp)

                _realWaterTemp.value = temp

                _snackbarMessage.emit(
                    "💧 Температура воды сохранена: ${
                        String.format(Locale.US, "%.1f", temp)
                    }°C"
                )

            } catch (_: Exception) {
                _snackbarMessage.emit(
                    "Ошибка сохранения температуры"
                )
            }
        }
    }




}