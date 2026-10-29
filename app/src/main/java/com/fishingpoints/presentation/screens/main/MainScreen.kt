package com.fishingpoints.presentation.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.core.network.FishingForecast
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.domain.permissions.LocationPermissionScreen
import com.fishingpoints.domain.permissions.rememberLocationPermissionState
import com.fishingpoints.presentation.screens.main.cards.BestTimesCard
import com.fishingpoints.presentation.screens.main.cards.ConditionsTextCard
import com.fishingpoints.presentation.screens.main.cards.FishRecommendationCard
import com.fishingpoints.presentation.screens.main.cards.LunarCard
import com.fishingpoints.presentation.screens.main.cards.RecommendationsCard
import com.fishingpoints.presentation.screens.main.cards.TimeSeasonCard
import com.fishingpoints.presentation.screens.main.cards.TipCard
import com.fishingpoints.presentation.screens.main.cards.WeatherCard
import com.fishingpoints.presentation.screens.main.component.FishingTimesChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val temperatureUnit by viewModel.temperatureUnit.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTemperatureUnit()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }

    }

    val forecast = uiState.fishingForecast

    // Получение разрешение на локацию.
    val permissionState = rememberLocationPermissionState(
        onPermissionGranted = { viewModel.start() },
        onPermissionDenied = { viewModel.onPermissionDenied() }
    )

    val isLoading = uiState.appState is AppState.LoadingLocation ||
            uiState.appState is AppState.LoadingWeather

    val isError = uiState.appState is AppState.Error

    val locationText = listOfNotNull(
        uiState.locationName,
        uiState.regionName
    ).joinToString(", ")

    Box(modifier = Modifier.fillMaxSize()) {
        val canShowMainScreen =
            permissionState.hasPermission ||
                    uiState.isUsingDefaultLocation

        if (!canShowMainScreen) {
            LocationPermissionScreen(
                permissionState = permissionState,
                onUseDefaultLocation = {
                    viewModel.useDefaultLocation()
                }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Рыболовный гид",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                if (locationText.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            text = locationText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Открыть меню"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

            ) { paddingValues ->
                if (!isLoading && !isError) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues( horizontal = 16.dp,
                            vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Прогноз Погоды
                        uiState.weather?.let { weather ->
                            item {
                                WeatherCard(
                                    weather = weather,
                                    isLoading = false,
                                    onRefresh = { viewModel.refreshForecast() },
                                    temperatureUnit = temperatureUnit,
                                    onUseRealWaterChanged = { enabled ->
                                        viewModel.setUseRealWaterTemperature(enabled)
                                    })
                            }
                        }
                        // Лунная информация
                        uiState.lunarInfo?.let { lunar ->
                            item { LunarCard(lunarInfo = lunar) }
                        }
                        // Время и сезон
                        item {
                            TimeSeasonCard(
                                timeRecommendation = uiState.dailyTips.timeRecommendation,
                                seasonRecommendation = uiState.dailyTips.seasonRecommendation
                            )
                        }

                        // Все общие временные интервалы.
                        item {
                            FishingTimesChart(
                                times = uiState.allFishingTimes
                            )
                        }

                        // Текущие условия
                        forecast?.let { fishingForecast ->
                            uiState.weather?.let { weather ->
                                item {
                                    ConditionsTextCard(
                                        text = buildConditionsText(
                                            forecast = fishingForecast,
                                            weather = weather
                                        )
                                    )
                                }
                            }
                        }
                        // Лучшее время для рыбалки
                        forecast?.let { fishingForecast ->
                            if (fishingForecast.bestTimes.isNotEmpty()) {
                                item {
                                    BestTimesCard(
                                        bestTimes = fishingForecast.bestTimes
                                    )
                                }
                            }
                        }

                        // Рекомендации
                        item {
                            RecommendationsCard(
                                tackleRecommendations = uiState.dailyTips.tackleRecommendations,
                                baitRecommendations = uiState.dailyTips.baitRecommendations,
                                groundbaitRecommendation = uiState.dailyTips.groundbaitRecommendation,
                                attractantRecommendation = uiState.dailyTips.attractantRecommendation
                            )
                        }
                        // Совет дня
                        if (uiState.dailyTips.tipOfTheDay.isNotBlank()) {
                            item {
                                TipCard(
                                    tip = uiState.dailyTips.tipOfTheDay
                                )
                            }
                        }

                        // Рекомендованные рыбы.
                        if (uiState.recommendedFish.isNotEmpty()) {

                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Рекомендуемые рыбы",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Виды, на которые стоит обратить внимание",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }


                            items(
                                items = uiState.recommendedFish,
                                key = { it.id }
                            ) { fish ->
                                FishRecommendationCard(
                                    fish = fish,
                                    onClick = {
                                        navController.navigate(
                                            "fish_forecast/${fish.id}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // LOADING OVERLAY
            if (isLoading) {
                LoadingOverlay(uiState.appState)
            }

            //  ERROR OVERLAY
            if (isError) {
                ErrorOverlay(
                    message = (uiState.appState as AppState.Error).message,
                    onRetry = { viewModel.refreshForecast() }
                )
            }

        }
    }

}

@Composable
fun ActivityIndicator(level: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .background(
                    color = when {
                        level >= 8 -> MaterialTheme.colorScheme.primaryContainer
                        level >= 5 -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$level/10",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    level >= 8 -> MaterialTheme.colorScheme.primary
                    level >= 5 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }

        Text(
            text = when {
                level >= 8 -> "Отлично"
                level >= 5 -> "Хорошо"
                level >= 3 -> "Слабо"
                else -> "Очень слабо"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoadingOverlay(state: AppState) {
    val text = when (state) {
        is AppState.LoadingLocation -> "Определяем местоположение"
        is AppState.LoadingWeather -> "Получаем погоду и \nанализируем клёв"
        else -> "Загрузка..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Spacer(Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state is AppState.LoadingWeather) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🐟 Подготавливаем прогноз",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}


@Composable
fun ErrorOverlay(
    message: String,
    onRetry: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Не удалось загрузить прогноз",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )


                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Попробовать ещё раз")
                }
            }
        }
    }
}


fun buildConditionsText(
    forecast: FishingForecast,
    weather: FishingWeather
): String {
    return buildString {
        appendLine("📍 ${forecast.location.name}")
        appendLine("🌅 Восход: ${forecast.dayInfo.sunrise}")
        appendLine("🌇 Закат: ${forecast.dayInfo.sunset}")
        appendLine("☀️ Полдень: ${forecast.dayInfo.solarNoon}")
        appendLine("⏰ Продолжительность дня: ${forecast.dayInfo.getDayLengthFormatted()}")
        appendLine("📅 Сезон: ${forecast.season.title}")
        appendLine("🎣 Клёв: %.1f/5".format(Locale.US,weather.biteRating))
        appendLine("📊 Активность: ${forecast.activityLevel}/10")
    }
}
