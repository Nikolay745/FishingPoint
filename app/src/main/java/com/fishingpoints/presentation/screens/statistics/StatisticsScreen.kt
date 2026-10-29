package com.fishingpoints.presentation.screens.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.presentation.formatting.formatWeight
import com.fishingpoints.presentation.screens.statistics.card.BestDayCard
import com.fishingpoints.presentation.screens.statistics.card.BestFishingTimeCard
import com.fishingpoints.presentation.screens.statistics.card.EmptyStatisticsCard
import com.fishingpoints.presentation.screens.statistics.card.StatisticsCard
import com.fishingpoints.presentation.screens.statistics.card.StatisticsRankingCard
import com.fishingpoints.presentation.screens.statistics.dialog.StatisticsMonthSelector
import java.time.YearMonth
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Статистика")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "Ошибка"
                    )
                }
            }

            else -> {
                StatisticsContent(
                    state = state,
                    onPeriodSelected = viewModel::setPeriod,
                    onMonthSelected = viewModel::setMonth,
                    onBestDayClick = {
                        viewModel.getEntryIdForBestDay { entryId ->
                            if (entryId != null) {
                                navController.navigate(
                                    "entry_detail/$entryId"
                                )
                            }
                        }
                    },

                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}


// ---------------------------------------------------------
// Основной контент
// ---------------------------------------------------------

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onBestDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = 6.dp,
            bottom = 24.dp
        )
    ) {

        // -------------------------------------------------
        // Выбор периода
        // -------------------------------------------------

        item {
            StatisticsPeriodSelector(
                selectedPeriod = state.period,
                onPeriodSelected = onPeriodSelected
            )
        }


        // -------------------------------------------------
        // Выбор конкретного месяца
        // -------------------------------------------------

        if (state.period == StatisticsPeriod.MONTH) {
            item {
                StatisticsMonthSelector(
                    selectedMonth = state.selectedMonth,
                    onMonthSelected = onMonthSelected
                )
            }
        }


        // -------------------------------------------------
        // Сводная статистика
        // -------------------------------------------------

        item {
            StatisticsSummary(
                fishingCount = state.fishingCount,
                totalFishCount = state.totalFishCount,
                totalWeight = state.totalWeight,
                averageFishPerFishing = state.averageFishPerFishing,
                averageFishWeight = state.averageFishWeight,
                averageWeightPerFishing = state.averageWeightPerFishing
            )
        }


        // -------------------------------------------------
        // График
        // -------------------------------------------------

        item {
            StatisticsChart(
                statistics = state.dailyStatistics,
                period = state.period
            )
        }


        // -------------------------------------------------
        // Лучший день
        // -------------------------------------------------

        item {
            BestDayCard(
                bestDay = state.bestDay,
                onClick = onBestDayClick
            )
        }


        // -------------------------------------------------
        // Лучшее время
        // -------------------------------------------------

        item {
            BestFishingTimeCard(
                bestFishingTime = state.bestFishingTime
            )
        }


        // -------------------------------------------------
        // Уловы по видам рыб
        // -------------------------------------------------

        item {
            Text(
                text = "Уловы по видам рыб",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (state.catches.isEmpty()) {

            item {
                EmptyStatisticsCard()
            }

        } else {

            items(
                items = state.catches,
                key = {
                    "fish_${it.fishId}"
                }
            ) { item ->

                CatchStatisticsItem(
                    item = item,
                    totalFishCount = state.totalFishCount
                )
            }
        }


        // -------------------------------------------------
        // Лучшие места
        // -------------------------------------------------

        if (state.locations.isNotEmpty()) {

            item {
                Text(
                    text = "Лучшие места",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = state.locations.take(5),
                key = {
                    "location_${it.locationId}"
                }
            ) { item ->

                StatisticsRankingCard(
                    title = "Место",
                    name = item.locationName,

                    subtitle =
                        item.regionName.ifBlank {
                            "${item.fishingCount} рыбалок"
                        },

                    fishCount = item.fishCount,
                    totalWeight = item.totalWeight
                )
            }
        }


        // -------------------------------------------------
        // Лучшие наживки
        // -------------------------------------------------

        if (state.baits.isNotEmpty()) {

            item {
                Text(
                    text = "Лучшие наживки",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = state.baits.take(5),
                key = {
                    "bait_${it.baitId}"
                }
            ) { item ->

                StatisticsRankingCard(
                    title = "Наживка",
                    name = item.baitName,
                    subtitle = "${item.fishingCount} рыбалок",
                    fishCount = item.fishCount,
                    totalWeight = item.totalWeight
                )
            }
        }


        // -------------------------------------------------
        // Лучшие снасти
        // -------------------------------------------------

        if (state.tackles.isNotEmpty()) {

            item {
                Text(
                    text = "Лучшие снасти",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = state.tackles.take(5),
                key = {
                    "tackle_${it.tackleId}"
                }
            ) { item ->

                StatisticsRankingCard(
                    title = "Снасть",
                    name = item.tackleName,
                    subtitle = "${item.fishingCount} рыбалок",
                    fishCount = item.fishCount,
                    totalWeight = item.totalWeight
                )
            }
        }


        // -------------------------------------------------
        // Лучшие прикормки
        // -------------------------------------------------

        if (state.feeds.isNotEmpty()) {

            item {
                Text(
                    text = "Лучшие прикормки",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = state.feeds.take(5),
                key = {
                    "feed_${it.feedId}"
                }
            ) { item ->

                StatisticsRankingCard(
                    title = "Прикормка",
                    name = item.feedName,
                    subtitle = "${item.fishingCount} рыбалок",
                    fishCount = item.fishCount,
                    totalWeight = item.totalWeight
                )
            }
        }
    }
}


// ---------------------------------------------------------
// Выбор периода
// ---------------------------------------------------------

@Composable
private fun StatisticsPeriodSelector(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {

        StatisticsPeriod.entries.forEachIndexed { index, period ->

            SegmentedButton(
                selected = selectedPeriod == period,

                onClick = {
                    onPeriodSelected(period)
                },

                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = StatisticsPeriod.entries.size
                )
            ) {
                Text(
                    text = period.label(),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}


private fun StatisticsPeriod.label(): String =
    when (this) {
        StatisticsPeriod.MONTH -> "Месяц"
        StatisticsPeriod.YEAR -> "Год"
        StatisticsPeriod.ALL_TIME -> "Всё время"
    }


@Composable
private fun StatisticsSummary(
    fishingCount: Int,
    totalFishCount: Long,
    totalWeight: Long,
    averageFishPerFishing: Double,
    averageFishWeight: Double,
    averageWeightPerFishing: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Рыбалок",
                value = fishingCount.toString(),
                icon = Icons.Default.Water
            )

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Рыб",
                value = totalFishCount.toString(),
                icon = Icons.Default.SetMeal
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Рыб / рыбалку",
                value = String.format(
                    LocalLocale.current.platformLocale,
                    "%.1f",
                    averageFishPerFishing
                ),
                icon = Icons.Default.Analytics
            )

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Средний вес",
                value = formatWeight(averageFishWeight.toLong()),
                icon = Icons.Default.MonitorWeight
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Вес / рыбалку",
                value = formatWeight(
                    averageWeightPerFishing.toLong()
                ),
                icon = Icons.Default.Timeline
            )

            StatisticsCard(
                modifier = Modifier.weight(1f),
                title = "Общий вес",
                value = formatWeight(totalWeight),
                icon = Icons.Default.Scale
            )

        }
    }
}


@Composable
private fun CatchStatisticsItem(
    item: CatchStatisticsRow,
    totalFishCount: Long
) {
    val percentage =
        if (totalFishCount > 0) {
            item.catchCount.toFloat() / totalFishCount
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Изображение рыбы
                FishImage(
                    imageUrl = item.imageUrl,
                    fishName = item.fishName
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = item.fishName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "${item.catchCount} рыб",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        text = formatWeight(item.totalWeight),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${(percentage * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FishImage(
    imageUrl: String?,
    fishName: String
) {
    val context = LocalContext.current

    val imageResId = remember(imageUrl) {
        imageUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { imageName ->
                context.resources.getIdentifier(
                    imageName,
                    "drawable",
                    context.packageName
                )
            }
            ?.takeIf { it != 0 }
    }

    Box(
        modifier = Modifier
            .width(96.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {

        if (imageResId != null) {

            Image(
                painter = painterResource(id = imageResId),
                contentDescription = fishName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        } else {

            Icon(
                imageVector = Icons.Default.SetMeal,
                contentDescription = fishName,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
