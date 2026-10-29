package com.fishingpoints.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fishingpoints.AppThemeViewModel
import com.fishingpoints.presentation.screens.settings.component.SettingsModernCard
import com.fishingpoints.presentation.screens.settings.component.SettingsRadioGroup
import com.fishingpoints.presentation.screens.settings.component.SettingsThemeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: AppThemeViewModel = hiltViewModel()
) {
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val measurementSystem by viewModel.measurementSystem.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Настройки") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsModernCard {
                    SettingsThemeSelector(
                        themeMode = themeMode,
                        onThemeSelected = themeViewModel::setThemeMode
                    )
                }
            }

            item {
                SettingsModernCard {
                    SettingsRadioGroup(
                        title = "Температура",
                        subtitle = "Единицы измерения температуры",
                        icon = Icons.Outlined.Thermostat,
                        options = listOf(
                            "Цельсий (°C)",
                            "Фаренгейт (°F)"
                        ),
                        selectedOption = when (temperatureUnit) {
                            TemperatureUnit.CELSIUS -> "Цельсий (°C)"
                            TemperatureUnit.FAHRENHEIT -> "Фаренгейт (°F)"
                        },
                        onOptionSelected = { option ->
                            viewModel.setTemperatureUnit(
                                when (option) {
                                    "Фаренгейт (°F)" -> TemperatureUnit.FAHRENHEIT
                                    else -> TemperatureUnit.CELSIUS
                                }
                            )
                        }
                    )
                }
            }

            item {
                SettingsModernCard {
                    SettingsRadioGroup(
                        title = "Система измерения",
                        subtitle = "Единицы длины и расстояния",
                        icon = Icons.Outlined.Straighten,
                        options = listOf(
                            "Метрическая",
                            "Имперская"
                        ),
                        selectedOption = when (measurementSystem) {
                            MeasurementSystem.METRIC -> "Метрическая"
                            MeasurementSystem.IMPERIAL -> "Имперская"
                        },
                        onOptionSelected = { option ->
                            viewModel.setMeasurementSystem(
                                when (option) {
                                    "Имперская" -> MeasurementSystem.IMPERIAL
                                    else -> MeasurementSystem.METRIC
                                }
                            )
                        }
                    )
                }
            }

            item {
                SettingsModernCard {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔔 Уведомления",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = viewModel::setNotificationsEnabled
                            )
                        }
                    }
                }
            }

            item {
                SettingsModernCard {
                    SettingsClickableCard(
                        title = "🌡️ Температура воды",
                        onClick = {
                            navController.navigate("water_temp_settings")
                        }
                    )
                }
            }
        }
    }
}

// Компонент для кликабельных пунктов настроек
@Composable
fun SettingsClickableCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

