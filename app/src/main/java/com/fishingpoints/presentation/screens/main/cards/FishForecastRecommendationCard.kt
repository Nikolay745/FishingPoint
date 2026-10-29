package com.fishingpoints.presentation.screens.main.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.presentation.screens.main.ActivityIndicator
import com.fishingpoints.presentation.screens.main.model.FishForecastRecommendation
import com.fishingpoints.presentation.screens.main.model.FishTimeForecast

@Composable
fun FishForecastRecommendationCard(
    forecast: FishForecastRecommendation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Название рыбы.
            Text(
                text = "🐟 ${forecast.fishName}",
                style = MaterialTheme.typography.titleLarge
            )

            HorizontalDivider()

            // Рейтинг и активность.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Рейтинг клёва",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = "%.1f / 5".format(forecast.rating),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                ActivityIndicator(
                    level = forecast.activityLevel
                )
            }

            Text(
                text = "ID рыбы: ${forecast.fishId}",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            // Причины итогового прогноза.
            Text(
                text = "Почему такой прогноз",
                style = MaterialTheme.typography.titleMedium
            )

            forecast.reasons.forEach { reason ->
                Text(
                    text = "• $reason",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Предпочтительное время суток.
            if (forecast.bestTimeOfDay.isNotEmpty()) {
                HorizontalDivider()

                Text(
                    text = "Предпочтительное время суток",
                    style = MaterialTheme.typography.titleMedium
                )

                forecast.bestTimeOfDay.forEach { time ->
                    Text(
                        text = "• ${time.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Лучшие интервалы для этой рыбы.
            if (forecast.bestTimes.isNotEmpty()) {
                HorizontalDivider()

                Text(
                    text = "Лучшие интервалы ловли",
                    style = MaterialTheme.typography.titleMedium
                )

                forecast.bestTimes.forEachIndexed { index, interval ->
                    FishTimeForecastItem(
                        index = index + 1,
                        interval = interval
                    )
                }
            }

            // Наживки.
            forecast.tipsBaits
                ?.takeIf { it.isNotBlank() }
                ?.let { baits ->
                    HorizontalDivider()

                    Text(
                        text = "Рекомендуемые наживки",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(baits)
                }

            // Снасти.
            forecast.tackles
                ?.takeIf { it.isNotBlank() }
                ?.let { tackles ->
                    HorizontalDivider()

                    Text(
                        text = "Рекомендуемые снасти",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(tackles)
                }

            // Приманки.
            forecast.baitForFish
                ?.takeIf { it.isNotBlank() }
                ?.let { bait ->
                    HorizontalDivider()

                    Text(
                        text = "Приманки для рыбы",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(bait)
                }
        }
    }
}

@Composable
fun FishTimeForecastItem(
    index: Int,
    interval: FishTimeForecast
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$index. ${interval.start} – ${interval.end}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Рейтинг клёва: %.1f / 5".format(interval.rating)
            )

            Text(
                text = "Активность: ${interval.activityLevel}/10"
            )

            ActivityIndicator(
                level = interval.activityLevel
            )

            Text(
                text = interval.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (interval.reasons.isNotEmpty()) {
                Text(
                    text = "Обоснование:",
                    style = MaterialTheme.typography.labelLarge
                )

                interval.reasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}