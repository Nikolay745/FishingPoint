package com.fishingpoints.presentation.screens.main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.presentation.formatting.formatTimeRange
import com.fishingpoints.presentation.screens.main.model.BestFishingTime

@Composable
fun FishingTimesChart(
    times: List<BestFishingTime>
) {
    var expanded by remember { mutableStateOf(false) }

    val sortedTimes = remember(times) {
        times.sortedBy { it.start }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {

                    Text(
                        text = "Прогноз клёва",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Лучшие периоды для рыбалки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${times.size} временных интервалов",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = if (expanded) "▲" else "▼"
                )
            }

            if (times.isEmpty()) {
                Text(
                    text = "Нет доступных интервалов",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val displayedTimes = if (expanded) {
                    sortedTimes
                } else {
                    sortedTimes.take(4)
                }

                displayedTimes.forEach { interval ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTimeRange(interval.start, interval.end),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "%.1f/5".format(interval.rating),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val ratingColor = when {
                            interval.rating >= 4.0 ->
                                MaterialTheme.colorScheme.primary

                            interval.rating >= 3.0 ->
                                MaterialTheme.colorScheme.tertiary

                            else ->
                                MaterialTheme.colorScheme.error
                        }

                        LinearProgressIndicator(
                            progress = {
                                (interval.rating / 5.0)
                                    .toFloat()
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = ratingColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )


                        Text(
                            text = "Активность: ${interval.activityLevel}/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (sortedTimes.size > 4) {
                    Text(
                        text = if (expanded) {
                            "Свернуть"
                        } else {
                            "Показать все интервалы (${sortedTimes.size})"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = !expanded
                            }
                            .padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}