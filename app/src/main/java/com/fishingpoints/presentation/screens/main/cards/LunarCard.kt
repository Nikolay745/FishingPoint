package com.fishingpoints.presentation.screens.main.cards

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.domain.lunar.LunarInfo
import java.util.Locale
import kotlin.text.format

@SuppressLint("DefaultLocale")
@Composable
fun LunarCard(lunarInfo: LunarInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🌙 Лунный календарь",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lunarInfo.phaseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Освещенность: ${String.format(Locale.US,"%.1f", lunarInfo.illumination)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Влияние на клев:", style = MaterialTheme.typography.bodyLarge)
                Row {
                    repeat(5) { index ->
                        Icon(
                            if (index < lunarInfo.biteRating) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < lunarInfo.biteRating)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Text(
                text = lunarInfo.recommendation,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}