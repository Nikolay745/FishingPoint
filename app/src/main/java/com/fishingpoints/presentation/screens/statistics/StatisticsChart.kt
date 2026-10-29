package com.fishingpoints.presentation.screens.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsChart(
    statistics: List<DailyCatchStatistics>,
    period: StatisticsPeriod,
    modifier: Modifier = Modifier
) {
    if (statistics.isEmpty()) {
        return
    }

    val chartStatistics = remember(
        statistics,
        period
    ) {
        when (period) {
            StatisticsPeriod.MONTH -> statistics
            StatisticsPeriod.YEAR -> groupStatisticsByMonth(statistics)
            StatisticsPeriod.ALL_TIME -> groupStatisticsByMonth(statistics)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Динамика улова",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (period) {
                    StatisticsPeriod.MONTH -> "Количество рыб по дням"
                    StatisticsPeriod.YEAR -> "Количество рыб по месяцам"
                    StatisticsPeriod.ALL_TIME -> "Количество рыб по месяцам"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val hasWeight =
                chartStatistics.any {
                    it.totalWeight > 0
                }

            FishCountChart(
                statistics = chartStatistics,
                period = period
            )
            if (hasWeight) {
                Spacer(modifier = Modifier.height(24.dp))
                WeightChart(
                    statistics = chartStatistics,
                    period = period
                )
            }
        }
    }
}

private fun groupStatisticsByMonth(
    statistics: List<DailyCatchStatistics>
): List<DailyCatchStatistics> {
    val zone = ZoneId.systemDefault()

    return statistics
        .groupBy { item ->
            Instant
                .ofEpochMilli(item.date)
                .atZone(zone)
                .toLocalDate()
                .withDayOfMonth(1)
        }
        .map { (month, items) ->

            DailyCatchStatistics(
                date = month
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli(),
                fishingCount = items.sumOf { it.fishingCount },
                fishCount = items.sumOf { it.fishCount },
                totalWeight = items.sumOf { it.totalWeight }
            )
        }
        .sortedBy {
            it.date
        }
}

@Composable
private fun rememberDateAxisFormatter(
    statistics: List<DailyCatchStatistics>,
    period: StatisticsPeriod
): CartesianValueFormatter {
    val locale = LocalLocale.current.platformLocale
    return remember(
        statistics,
        period,
        locale
    ) {
        CartesianValueFormatter { _, value, _ ->

            val index = value.toInt()
            if (index !in statistics.indices) {
                return@CartesianValueFormatter ""
            }

            if (
                !shouldShowXAxisLabel(
                    index = index,
                    size = statistics.size,
                    maxLabels = 7
                )
            ) {
                return@CartesianValueFormatter ""
            }

            val date = Instant
                .ofEpochMilli(statistics[index].date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            when (period) {
                StatisticsPeriod.MONTH ->
                    date.format(
                        DateTimeFormatter.ofPattern(
                            "dd.MM",
                            locale
                        )
                    )

                StatisticsPeriod.YEAR ->
                    date.format(
                        DateTimeFormatter.ofPattern(
                            "MMM",
                            locale
                        )
                    )

                StatisticsPeriod.ALL_TIME ->
                    date.format(
                        DateTimeFormatter.ofPattern(
                            "MM.yyyy",
                            locale
                        )
                    )
            }
        }
    }
}

@Composable
private fun FishCountChart(
    statistics: List<DailyCatchStatistics>,
    period: StatisticsPeriod
) {
    if (statistics.isEmpty()) {
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val xAxisFormatter = rememberDateAxisFormatter(
        statistics = statistics,
        period = period
    )

    val yAxisStep = remember(statistics) {
        getFishCountAxisStep(statistics)
    }


    LaunchedEffect(statistics) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = statistics.indices.map {
                        it.toFloat()
                    },
                    y = statistics.map {
                        it.fishCount.toFloat()
                    }
                )
            }
        }
    }

    Text(
        text = "Количество рыб",
        style = MaterialTheme.typography.labelLarge
    )

    Spacer(modifier = Modifier.height(8.dp))


    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                itemPlacer = VerticalAxis.ItemPlacer.step(
                    step = { yAxisStep }
                ),
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    value.toInt().toString()
                }
            ),

            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xAxisFormatter),
            getXStep = { 1.0 }
        ),

        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}


@Composable
private fun WeightChart(
    statistics: List<DailyCatchStatistics>,
    period: StatisticsPeriod
) {
    if (statistics.isEmpty()) {
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val xAxisFormatter = rememberDateAxisFormatter(
        statistics = statistics,
        period = period
    )

    LaunchedEffect(statistics) {

        modelProducer.runTransaction {

            columnSeries {

                series(
                    x = statistics.indices.map {
                        it.toFloat()
                    },
                    y = statistics.map {
                        it.totalWeight.toFloat()/1000
                    }
                )
            }
        }
    }

    Text(
        text = "Общий вес, кг",
        style = MaterialTheme.typography.labelLarge
    )

    Spacer(modifier = Modifier.height(8.dp))

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisFormatter
            ),
            getXStep = { 1.0 }
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

// функция расчёта подписей
private fun shouldShowXAxisLabel(
    index: Int,
    size: Int,
    maxLabels: Int = 7
): Boolean {
    if (size <= maxLabels) {
        return true
    }

    // Всегда показываем первую и последнюю точку.
    if (index == 0 || index == size - 1) {
        return true
    }

    val step = (size - 1).toFloat() / (maxLabels - 1)
    val expectedIndex = index / step
    return kotlin.math.abs(
        expectedIndex - expectedIndex.toInt()
    ) < 0.15f
}

private fun getFishCountAxisStep(
    statistics: List<DailyCatchStatistics>
): Double {
    val maxValue = statistics.maxOfOrNull {
        it.fishCount
    } ?: 0

    return when {
        maxValue <= 10 -> 1.0
        maxValue <= 20 -> 2.0
        maxValue <= 50 -> 5.0
        maxValue <= 100 -> 10.0
        maxValue <= 200 -> 20.0
        maxValue <= 500 -> 50.0
        else -> 100.0
    }
}

