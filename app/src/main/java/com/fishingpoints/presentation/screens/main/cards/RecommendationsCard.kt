package com.fishingpoints.presentation.screens.main.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecommendationsCard(
    tackleRecommendations: List<String>,
    baitRecommendations: List<String>,
    groundbaitRecommendation: String,
    attractantRecommendation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎯 Рекомендации",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Снасти
            if (tackleRecommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🎣 Снасти",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tackleRecommendations) { recommendation ->
                            AssistChip(
                                onClick = { },
                                label = { Text(recommendation) }
                            )
                        }
                    }
                }
            }

            // Наживки
            if (baitRecommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🪱 Наживки",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(baitRecommendations) { recommendation ->
                            AssistChip(
                                onClick = { },
                                label = { Text(recommendation) }
                            )
                        }
                    }
                }
            }

            // Прикормка
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🍚 Прикормка",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = groundbaitRecommendation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Ароматизаторы
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "👃 Ароматизаторы",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = attractantRecommendation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}