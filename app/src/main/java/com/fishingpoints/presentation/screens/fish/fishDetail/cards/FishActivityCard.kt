package com.fishingpoints.presentation.screens.fish.fishDetail.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fishingpoints.domain.service.ActivityEstimateType
import com.fishingpoints.domain.service.FishActivityResult

@Composable
fun FishActivityCard(
    result: FishActivityResult?,
    waterTemperature: Double?,
    isEstimatedTemperature: Boolean,
    isLoading: Boolean
) {
    InfoCard("🐟 Активность питания") {

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }

            result == null -> {
                Text("Недостаточно данных для расчёта активности")
            }

            result.activity == null -> {
                Text(
                    text = result.description
                        ?: "Нет подходящих правил активности"
                )
            }

            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = result.activity.displayText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(8.dp))

                waterTemperature?.let {
                    Text(
                        text = "🌡 Температура воды: %.1f °C%s".format(
                            it,
                            if (isEstimatedTemperature) " (расчётная)" else ""
                        )
                    )
                }

                result.description?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                when (result.estimateType) {

                    ActivityEstimateType.RULE_MATCHED -> {
                        Text(
                            text = "✓ Найдено подходящее правило активности",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    ActivityEstimateType.PRELIMINARY -> {
                        Text(
                            text = "⚠ Предварительная оценка",
                            color = Color(0xFFE69A00),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Использовано общее температурное правило. " +
                                    "Сезонные особенности не учтены.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ActivityEstimateType.NONE -> Unit
                }


                result.confidence?.displayName?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Достоверность правила: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
