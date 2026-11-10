package com.fishingpoints.presentation.screens.main.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.domain.recommendation.adviceToDay.FishingTipResult
import com.fishingpoints.domain.recommendation.adviceToDay.ForecastConfidenceLevel

@Composable
fun FishingRecommendationsCard(
    tip: FishingTipResult?
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Заголовок
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = colors.primary
                )

                Text(
                    text = "Рекомендации на сегодня",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tip == null) {
                Text(
                    text = "Рекомендации пока недоступны.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                return@Column
            }

            // Совет дня
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = tip.title.ifBlank { "💡 Совет дня" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onTertiaryContainer
                    )

                    Text(
                        text = tip.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onTertiaryContainer
                    )
                }
            }

            // Условия ловли
            val conditions = buildList {

                tip.fishName?.let {
                    add("🐟 Рыба: $it")
                }

                tip.recommendedTime?.let {
                    add("🕒 Время: $it")
                }

                tip.recommendedBait?.let {
                    add("🪱 Наживка: $it")
                }

                tip.recommendedTackle?.let {
                    add("🎣 Снасти: $it")
                }
            }


            if (conditions.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Условия ловли",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    conditions.forEach { condition ->
                        Text(
                            text = condition,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Уверенность
            tip.confidence?.let { confidence ->

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "📊 Достоверность прогноза",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Достоверность: ${
                            when (confidence.level) {
                                ForecastConfidenceLevel.VERY_HIGH -> "Очень высокая"
                                ForecastConfidenceLevel.HIGH -> "Высокая"
                                ForecastConfidenceLevel.MEDIUM -> "Средняя"
                                ForecastConfidenceLevel.LOW -> "Низкая"
                                ForecastConfidenceLevel.VERY_LOW -> "Очень низкая"
                            }
                        }"
                    )



                    Text(
                        text = "Оценка: ${confidence.score}%"
                    )

                    Text(
                        text = "Доступность данных: " +
                                "${confidence.availableDataPercent}%"
                    )
                }
            }


            // Причины рекомендации
            if (tip.reasons.isNotEmpty()) {
                HorizontalDivider()

                Text(
                    text = "Почему эта рекомендация?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                tip.reasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationSection(
    title: String,
    items: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                AssistChip(
                    onClick = {},
                    label = { Text(item) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationText(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
