package com.fishingpoints

import androidx.lifecycle.ViewModel
import com.fishingpoints.data.repository.WeatherRepository
import com.fishingpoints.presentation.screens.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _themeMode = MutableStateFlow(
        weatherRepository.getThemeMode()
    )

    val themeMode: StateFlow<ThemeMode> =
        _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        if (_themeMode.value == mode) return

        weatherRepository.setThemeMode(mode)
        _themeMode.value = mode
    }
}
