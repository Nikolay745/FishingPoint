package com.fishingpoints.presentation.screens.diary.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.presentation.screens.diary.CatchWithDetails

@Composable
fun SummaryCard(catches: List<CatchWithDetails>) {
    val totalCount = catches.sumOf { it.catch.count }
    val totalWeight = catches.sumOf { it.catch.totalWeight ?: 0}
    val totalSpecies = catches
        .map { it.catch.fishId }
        .distinct()
        .size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📊 Итоги рыбалки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "🎣 Видов рыбы: $totalSpecies",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "🐟 Всего рыбы: $totalCount шт.",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "⚖️ Общий вес: ${
                    String.format(
                        LocalLocale.current.platformLocale,
                        "%.2f",
                        totalWeight / 1000.0
                    )
                } кг",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (catches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🏆 Самый большой улов:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val biggestCatch = remember(catches) {
                    catches.maxByOrNull {
                        it.catch.totalWeight ?: 0
                    }
                }

                biggestCatch?.let { catchItem ->
                    val weight = catchItem.catch.totalWeight ?: 0

                    Text(
                        text = "${catchItem.fishName} - ${
                            String.format(
                                LocalLocale.current.platformLocale,
                                "%.2f",
                                weight / 1000.0
                            )
                        } кг (${catchItem.catch.count} шт.)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
