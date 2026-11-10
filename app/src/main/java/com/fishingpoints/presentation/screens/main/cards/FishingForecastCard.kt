package com.fishingpoints.presentation.screens.main.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.models.DailyFishingForecast
import com.fishingpoints.presentation.formatting.formatTemperature
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FishingForecastCard(
    forecasts: List<DailyFishingForecast>,
    temperatureUnit: TemperatureUnit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Прогноз на 5 дней",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            forecasts.forEach { forecast ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = forecast.date.format(
                                DateTimeFormatter.ofPattern(
                                    "EEEE, d MMMM",
                                    Locale("ru")
                                )
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = forecast.precipitation.replaceFirstChar {
                                it.uppercase()
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = getWeatherIcon(
                            forecast.weatherId
                        ),
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${formatTemperature(forecast.temperatureMax, temperatureUnit)} / ${
                                formatTemperature(forecast.temperatureMin, temperatureUnit)
                            }",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ветер %.1f м/с".format(
                                Locale.US,
                                forecast.windSpeed
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )

                        Text(
                            text = "${String.format(Locale.US, "%.1f", forecast.windSpeed)} м/с ${
                                getWindDirection(forecast.windDirection)
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )


                    }
                }

                LinearProgressIndicator(
                    progress = {
                        (forecast.biteRating / 5.0)
                            .coerceIn(0.0, 1.0)
                            .toFloat()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )

                Text(
                    text = "Активность клёва: ${
                        forecast.biteRating.toInt()
                    }/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                HorizontalDivider(
                    color = colors.outlineVariant
                )
            }
        }
    }
}
