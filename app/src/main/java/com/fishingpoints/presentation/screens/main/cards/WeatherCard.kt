package com.fishingpoints.presentation.screens.main.cards

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.presentation.formatting.formatTemperature
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.models.PressureTrend
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun WeatherCard(
    weather: FishingWeather,
    isLoading: Boolean,
    temperatureUnit: TemperatureUnit,
    onRefresh: () -> Unit,
    onUseRealWaterChanged: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            colors.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getWeatherIcon(weather.weatherId),
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Текущая погода",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Условия для рыбалки",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить погоду"
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {

                // Температура воздуха
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Температура воздуха",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPrimaryContainer.copy(
                                alpha = 0.8f
                            )
                        )

                        Text(
                            text = formatTemperature(
                                temperatureCelsius = weather.temperature,
                                unit = temperatureUnit
                            ),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )
                    }
                }

                // Температура воды
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant.copy(
                            alpha = 0.55f
                        )
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Water,
                                contentDescription = null,
                                tint = colors.primary
                            )

                            Spacer(Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Температура воды",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Text(
                                    text = if (
                                        weather.isRealWaterTemperature
                                    ) {
                                        "Фактические данные"
                                    } else {
                                        "Расчётное значение"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }

                            Text(
                                text = formatTemperature(
                                    temperatureCelsius = weather.waterTemperature,
                                    unit = temperatureUnit
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(
                            color = colors.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Использовать реальную температуру",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Switch(
                                checked = weather.isRealWaterTemperature,
                                onCheckedChange = onUseRealWaterChanged
                            )
                        }
                    }
                }

                // Основные показатели
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WeatherMetric(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        title = "Давление",
                        value = if (weather.pressure > 0) {
                            "${weather.pressure}"
                        } else {
                            "Нет данных"
                        },
                        subtitle = if (weather.pressure > 0) {
                            when (weather.pressureTrend) {
                                PressureTrend.RISING -> "↑ Растёт"
                                PressureTrend.FALLING -> "↓ Падает"
                                PressureTrend.STABLE -> "→ Стабильно"
                                PressureTrend.NODATA -> "Нет данных"
                            }
                        } else {
                            ""
                        }
                    )

                    WeatherMetric(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Air,
                        title = "Ветер",
                        value = String.format(
                            Locale.US,
                            "%.1f м/с",
                            weather.windSpeed
                        ),
                        subtitle = "${getWindDirection(weather.windDirection)} · ${weather.windDirection}°"
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WeatherMetric(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Cloud,
                        title = "Облачность",
                        value = "${weather.cloudiness}%",
                        subtitle = "Покрытие неба"
                    )

                    WeatherMetric(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        title = "Осадки",
                        value = weather.precipitation,
                        subtitle = "Сейчас"
                    )
                }

                // Рейтинг клёва
                HorizontalDivider(
                    color = colors.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Активность клёва",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = when {
                                weather.biteRating >= 4 -> "Отличные условия"
                                weather.biteRating >= 3 -> "Хорошие условия"
                                weather.biteRating >= 2 -> "Умеренная активность"
                                else -> "Низкая активность"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (
                                    index < weather.biteRating
                                ) {
                                    Icons.Default.Star
                                } else {
                                    Icons.Default.StarBorder
                                },
                                contentDescription = null,
                                tint = if (
                                    index < weather.biteRating
                                ) {
                                    colors.primary
                                } else {
                                    colors.outlineVariant
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Рекомендация
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.tertiaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = colors.onTertiaryContainer
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Рекомендация",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onTertiaryContainer
                            )

                            Text(
                                text = weather.recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(
                alpha = 0.55f
            )
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
fun getWindDirection(degrees: Int): String {
    val normalized = ((degrees % 360) + 360) % 360

    return when (normalized) {
        in 338..359, in 0..22 -> "С"
        in 23..67 -> "СВ"
        in 68..112 -> "В"
        in 113..157 -> "ЮВ"
        in 158..202 -> "Ю"
        in 203..247 -> "ЮЗ"
        in 248..292 -> "З"
        in 293..337 -> "СЗ"
        else -> "—"
    }
}


fun getWeatherIcon(weatherId: Int): ImageVector {
    return when (weatherId) {
        in 200..232 -> Icons.Default.Thunderstorm
        in 300..321 -> Icons.Default.Cloud
        in 500..531 -> Icons.Default.WaterDrop
        in 600..622 -> Icons.Default.AcUnit
        in 701..781 -> Icons.Default.Dehaze
        800 -> Icons.Default.WbSunny
        801 -> Icons.Default.CloudQueue
        in 802..804 -> Icons.Default.Cloud
        else -> Icons.Default.Cloud
    }
}


