package com.fishingpoints.presentation.screens.statistics.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.chunked
import kotlin.collections.forEach


// ---------------------------------------------------------
// Диалог выбора месяца
// ---------------------------------------------------------

@Composable
fun MonthPickerDialog(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val currentMonth = remember {
        YearMonth.now()
    }

    var displayedYear by remember(
        selectedMonth.year
    ) {
        mutableIntStateOf(
            selectedMonth.year
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        displayedYear--
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription =
                            "Предыдущий год"
                    )
                }


                Text(
                    text = displayedYear.toString(),
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(
                    onClick = {
                        if (displayedYear < currentMonth.year) {
                            displayedYear++
                        }
                    },

                    enabled =  displayedYear < currentMonth.year
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowForward,

                        contentDescription =
                            "Следующий год"
                    )
                }
            }
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Month.entries
                    .toList()
                    .chunked(3)
                    .forEach { rowMonths ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            rowMonths.forEach { month ->

                                val yearMonth =
                                    YearMonth.of(
                                        displayedYear,
                                        month
                                    )

                                val isFuture =
                                    yearMonth > currentMonth

                                val isSelected =
                                    yearMonth == selectedMonth


                                FilterChip(
                                    selected = isSelected,

                                    onClick = {
                                        if (!isFuture) {
                                            onMonthSelected(
                                                yearMonth
                                            )
                                        }
                                    },

                                    enabled = !isFuture,
                                    label = {
                                        Text(
                                            text =
                                                month.shortName()
                                        )
                                    },

                                    modifier =
                                        Modifier.weight(1f)
                                )
                            }
                        }
                    }
            }
        },

        confirmButton = {}
    )
}
// ---------------------------------------------------------
// Выбор месяца
// ---------------------------------------------------------

@Composable
 fun StatisticsMonthSelector(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern(
            "LLLL yyyy",
            Locale("ru", "RU")
        )
    }

    OutlinedButton(
        onClick = {
            showDialog = true
        },

        modifier = Modifier.fillMaxWidth()
    ) {

        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = selectedMonth
                .format(monthFormatter)
                .replaceFirstChar {
                    it.uppercase()
                }
        )
    }


    if (showDialog) {

        MonthPickerDialog(
            selectedMonth = selectedMonth,

            onMonthSelected = { month ->
                onMonthSelected(month)
                showDialog = false
            },

            onDismiss = {
                showDialog = false
            }
        )
    }
}

private fun Month.shortName(): String =
    when (this) {
        Month.JANUARY -> "Янв"
        Month.FEBRUARY -> "Фев"
        Month.MARCH -> "Мар"
        Month.APRIL -> "Апр"
        Month.MAY -> "Май"
        Month.JUNE -> "Июн"
        Month.JULY -> "Июл"
        Month.AUGUST -> "Авг"
        Month.SEPTEMBER -> "Сен"
        Month.OCTOBER -> "Окт"
        Month.NOVEMBER -> "Ноя"
        Month.DECEMBER -> "Дек"
    }