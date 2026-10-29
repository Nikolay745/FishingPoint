package com.fishingpoints.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.fishingpoints.presentation.formatting.formatTemperature
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTemperatureSettingsScreen(
    navController: NavController,
    viewModel: WeatherSettingsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val useRealWaterTemp by viewModel.useRealWaterTemp.collectAsState()
    val realWaterTemp by viewModel.realWaterTemp.collectAsState()
    // Выбранная единица измерения температуры
    val temperatureUnit by settingsViewModel.temperatureUnit.collectAsState()

    var showTempDialog by remember { mutableStateOf(false) }


    val temperatureSymbol = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "°C"
        TemperatureUnit.FAHRENHEIT -> "°F"
    }



    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("🌡️ Настройки температуры воды") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Режим измерения
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Режим измерения температуры воды",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = !useRealWaterTemp,
                                onClick = { viewModel.setUseRealWaterTemp(false) }
                            )
                            Text("📊 Расчетная (по температуре воздуха)")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = useRealWaterTemp,
                                onClick = { viewModel.setUseRealWaterTemp(true) }
                            )
                            Text("🌡️ Реальная (измеренная термометром)")
                        }
                    }
                }
            }

            // Ввод реальной температуры
            if (useRealWaterTemp) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Измеренная температура воды",
                                style = MaterialTheme.typography.titleMedium
                            )


                            Text(
                                text = "Текущее значение: ${
                                    formatTemperature(realWaterTemp, temperatureUnit)
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = { showTempDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Изменить температуру")
                            }

                            Text(
                                text = "Измерьте температуру воды термометром на глубине 0.5-1м",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Информация
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ℹ️ О влиянии температуры воды",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
//                        Text(
//                            text = "• Оптимальная температура для клева: +10°C до +20°C\n" +
//                                    "• При температуре ниже +8°C рыба малоактивна\n" +
//                                    "• При температуре выше +22°C рыба уходит в глубину\n" +
//                                    "• Резкие перепады температуры снижают клев",
//                            style = MaterialTheme.typography.bodySmall
//                        )
                        Text(
                            text = if (temperatureUnit == TemperatureUnit.CELSIUS) {
                                "• Оптимальная температура для клева: +10°C до +20°C\n" +
                                        "• При температуре ниже +8°C рыба малоактивна\n" +
                                        "• При температуре выше +22°C рыба уходит в глубину\n" +
                                        "• Резкие перепады температуры снижают клев"
                            } else {
                                "• Оптимальная температура для клева: +50°F до +68°F\n" +
                                        "• При температуре ниже +46.4°F рыба малоактивна\n" +
                                        "• При температуре выше +71.6°F рыба уходит в глубину\n" +
                                        "• Резкие перепады температуры снижают клев"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )

                    }
                }
            }

            // Кнопка обновления погоды
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔄 Применить настройки",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "После изменения настроек вернитесь на главный экран и нажмите кнопку обновления",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Готово")
                        }
                    }
                }
            }
        }
    }

    if (showTempDialog) {
        TemperatureInputDialog(
            currentTemp = realWaterTemp,
            temperatureUnit = temperatureUnit,
            onDismiss = { showTempDialog = false },
            onSave = { newTemp ->
                viewModel.saveRealWaterTemp(newTemp)
                showTempDialog = false

            }
        )
    }
}

//
//@Composable
//fun TemperatureInputDialog(
//    currentTemp: Double,
//    onDismiss: () -> Unit,
//    onSave: (Double) -> Unit
//) {
//
//    var temp by remember(currentTemp) {
//        mutableStateOf(
//            String.format(Locale.US, "%.1f", currentTemp)
//        )
//    }
//
//    var errorMessage by remember {
//        mutableStateOf<String?>(null)
//    }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//
//        title = {
//            Text("Введите температуру воды")
//        },
//
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//
//                OutlinedTextField(
//                    value = temp,
//
//                    onValueChange = {
//                        temp = it.replace(',', '.')
//
//                        errorMessage = null
//                    },
//
//                    label = {
//                        Text("Температура воды (°C)")
//                    },
//
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Decimal
//                    ),
//
//                    isError = errorMessage != null,
//
//                    supportingText = {
//                        errorMessage?.let {
//                            Text(
//                                text = it,
//                                color = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                )
//            }
//        },
//
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    when (val tempValue = temp.toDoubleOrNull()) {
//                        null -> {
//                            errorMessage = "Введите число"
//                        }
//                        !in -2.0..35.0 -> {
//                            errorMessage = "Температура должна быть от -2 до 35"
//                        }
//                        else -> {
//                            onSave(tempValue)
//                        }
//                    }
//                }
//            ) {
//                Text("Сохранить")
//            }
//        },
//
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Отмена")
//            }
//        }
//    )
//}

@Composable
fun TemperatureInputDialog(
    currentTemp: Double,
    temperatureUnit: TemperatureUnit,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    // Храним температуру в Цельсиях,
    // но показываем значение в выбранной единице измерения.
    val displayTemp = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> currentTemp
        TemperatureUnit.FAHRENHEIT -> currentTemp * 9.0 / 5.0 + 32.0
    }

    var temp by remember(currentTemp, temperatureUnit) {
        mutableStateOf(
            String.format(Locale.US, "%.1f", displayTemp)
        )
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val temperatureSymbol = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "°C"
        TemperatureUnit.FAHRENHEIT -> "°F"
    }

    // Допустимый диапазон в зависимости от единицы измерения.
    val minTemp = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> -2.0
        TemperatureUnit.FAHRENHEIT -> 28.4
    }

    val maxTemp = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> 35.0
        TemperatureUnit.FAHRENHEIT -> 95.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Введите температуру воды")
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = temp,

                    onValueChange = {
                        temp = it.replace(',', '.')
                        errorMessage = null
                    },

                    label = {
                        Text("Температура воды ($temperatureSymbol)")
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),

                    singleLine = true,

                    isError = errorMessage != null,

                    supportingText = {
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val inputTemp = temp.toDoubleOrNull()

                    when {
                        inputTemp == null || !inputTemp.isFinite() -> {
                            errorMessage = "Введите корректное число"
                        }

                        inputTemp !in minTemp..maxTemp -> {
                            errorMessage =
                                "Температура должна быть от " +
                                        "$minTemp до $maxTemp $temperatureSymbol"
                        }

                        else -> {
                            // Преобразуем введённую температуру
                            // обратно в Цельсии перед сохранением.
                            val tempCelsius = when (temperatureUnit) {
                                TemperatureUnit.CELSIUS -> inputTemp

                                TemperatureUnit.FAHRENHEIT ->
                                    (inputTemp - 32.0) * 5.0 / 9.0
                            }

                            onSave(tempCelsius)
                        }
                    }
                }
            ) {
                Text("Сохранить")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
