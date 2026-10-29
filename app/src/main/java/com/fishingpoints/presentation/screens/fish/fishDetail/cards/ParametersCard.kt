package com.fishingpoints.presentation.screens.fish.fishDetail.cards

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fishingpoints.R
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.presentation.screens.fish.components.getFeedingActivityIcon

@Composable
fun ParametersCard(fish: Fish) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📊 Ключевые параметры", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ParameterItem(
                    iconRes = R.drawable.thermometer,
                    label = "Температура",
                    value = "${fish.temperatureMin}°C - ${fish.temperatureMax}°C"
                )
                ParameterItem(
                    iconRes = R.drawable.gauge,
                    label = "Давление",
                    value = "${fish.pressureMin} - ${fish.pressureMax} мм рт. ст."
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                SmallParamCard(
                    title = "Сезон",
                    items = getSeasonItems(fish.seasons),
                    modifier = Modifier.weight(1f)
                )

                SmallParamCard(
                    title = "Время суток",
                    items = getTimeOfDayItems(fish.bestTimeOfDay),
                    modifier = Modifier.weight(1f)
                )

                SmallParamCard(
                    title = "Активность",
                    items = listOf(getFeedingActivityIcon(fish.feedingActivity)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ParameterItem(@DrawableRes iconRes: Int, label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val regularSeasons = setOf(
    Season.SPRING,
    Season.SUMMER,
    Season.AUTUMN,
    Season.WINTER
)

fun getSeasonItems(seasons: List<Season>?): List<String> {
    val selected = seasons.orEmpty().distinct()

    if (selected.isEmpty()) {
        return listOf("🌍 Все сезоны")
    }

    if (Season.ALL_YEAR in selected) {
        return listOf(Season.ALL_YEAR.displayName)
    }

    if (selected.containsAll(regularSeasons)) {
        return listOf("🌍 Все сезоны")
    }

    return selected.map { it.displayName }
}

fun getTimeOfDayItems(times: List<TimeOfDay>?): List<String> =
    times
        .orEmpty()
        .distinct()
        .map { it.displayName }
        .ifEmpty { listOf("⏰ Не указано") }




