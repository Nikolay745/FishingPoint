package com.fishingpoints.presentation.screens.fishingRule.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.Region
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishingBanDialog(
    fishList: List<Fish>,
    regionList: List<Region>,
    selectedRegionId: Long?,
    selectedDate: LocalDate,
    ban: FishingBan? = null,
    onDismiss: () -> Unit,
    onSave: (FishingBan) -> Unit
) {
    val initialRegionId = ban?.regionId ?: selectedRegionId
    var selectedRegionIdLocal by rememberSaveable(
        ban?.id, selectedRegionId
    ) {
        mutableStateOf(initialRegionId)
    }

    var selectedFishIds by remember(ban?.id) {
        mutableStateOf(ban?.fishIds?.toSet() ?: emptySet())
    }
    var title by rememberSaveable(ban?.id) { mutableStateOf(ban?.title ?: "") }
    var description by rememberSaveable(ban?.id) { mutableStateOf(ban?.description ?: "") }
    var location by rememberSaveable(ban?.id) { mutableStateOf(ban?.location ?: "") }

    val localDateSaver = Saver<LocalDate, String>(
        save = { it.toString() },
        restore = { LocalDate.parse(it) }
    )
    // Период действия записи
    var validFrom by rememberSaveable(
        ban?.id,
        selectedDate,
        stateSaver = localDateSaver
    ) {
        mutableStateOf(ban?.validFrom ?: selectedDate)
    }

    var validUntil by rememberSaveable(
        ban?.id,
        selectedDate,
        stateSaver = Saver(
            save = { it?.toString() },
            restore = { it.let(LocalDate::parse) }
        )
    ) {
        mutableStateOf(ban?.validUntil)
    }
    var seasonStartMonth by rememberSaveable(ban?.id) {
        mutableIntStateOf(ban?.seasonStartMonth ?: 4)
    }

    var seasonStartDay by rememberSaveable(ban?.id) {
        mutableIntStateOf(ban?.seasonStartDay ?: 1)
    }

    var seasonEndMonth by rememberSaveable(ban?.id) {
        mutableIntStateOf(ban?.seasonEndMonth ?: 6)
    }

    var seasonEndDay by rememberSaveable(ban?.id) {
        mutableIntStateOf(ban?.seasonEndDay ?: 10)
    }

    var fishExpanded by remember { mutableStateOf(false) }
    var regionExpanded by remember { mutableStateOf(false) }
    var showSeasonStartDialog by remember {
        mutableStateOf(false)
    }

    var showSeasonEndDialog by remember {
        mutableStateOf(false)
    }
    var showValidFromDialog by remember { mutableStateOf(false) }
    var showValidUntilDialog by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable(ban?.id) { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val selectedRegion = regionList.firstOrNull {
        it.id == selectedRegionIdLocal
    }
    val selectedFishNames = fishList
        .filter { it.id in selectedFishIds }
        .map { it.name }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (ban == null) {
                    "Добавить запрет"
                } else {
                    "Редактировать запрет"
                }
            )
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val regionId = selectedRegionIdLocal

                    if (
                        regionId == null ||
                        selectedRegion == null
                    ) {
                        errorMessage = "Выберите регион"
                        return@TextButton
                    }

                    if (title.isBlank()) {
                        errorMessage = "Введите название запрета"
                        return@TextButton
                    }

                    // Проверяем период действия записи
                    if (
                        validUntil != null &&
                        validUntil!!.isBefore(validFrom)
                    ) {
                        errorMessage =
                            "Дата окончания действия не может быть раньше даты начала"
                        return@TextButton
                    }

                    // Проверяем начало сезона
                    if (!isValidSeasonDate(seasonStartMonth, seasonStartDay)) {
                        errorMessage = "Некорректная дата начала сезона"
                        return@TextButton
                    }

                    // Проверяем окончание сезона
                    if (!isValidSeasonDate(seasonEndMonth, seasonEndDay)) {
                        errorMessage = "Некорректная дата окончания сезона"
                        return@TextButton
                    }

                    // Проверяем совпадение сезонных дат
                    if (
                        seasonStartMonth == seasonEndMonth &&
                        seasonStartDay == seasonEndDay
                    ) {
                        errorMessage = "Начало и окончание сезона не могут совпадать"
                        return@TextButton
                    }

                    val fishingBan = FishingBan(
                        id = ban?.id ?: 0L,
                        regionId = regionId,
                        validFrom = validFrom,
                        validUntil = validUntil,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        location = location.trim().ifBlank { null },
                        fishIds = selectedFishIds.toList(),
                        seasonStartMonth = seasonStartMonth,
                        seasonStartDay = seasonStartDay,
                        seasonEndMonth = seasonEndMonth,
                        seasonEndDay = seasonEndDay
                    )

                    // Убираем сообщение об ошибке
                    errorMessage = null

                    // Передаем данные родительскому Composable
                    onSave(fishingBan)
                }
            ) {
                Text(
                    if (ban == null) "Сохранить" else "Обновить"
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },

        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // РЕГИОН
                ExposedDropdownMenuBox(
                    expanded = regionExpanded,
                    onExpandedChange = {
                        regionExpanded = !regionExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedRegion?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Регион") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = regionExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = regionExpanded,
                        onDismissRequest = {
                            regionExpanded = false
                        }
                    ) {
                        regionList.forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region.name) },
                                onClick = {
                                    selectedRegionIdLocal = region.id
                                    regionExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                // РЫБА
                ExposedDropdownMenuBox(
                    expanded = fishExpanded,
                    onExpandedChange = {
                        fishExpanded = !fishExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = if (selectedFishIds.isEmpty()) {
                            "Все виды рыб"
                        } else {
                            selectedFishNames.joinToString(", ")
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Рыбы")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = fishExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = fishExpanded,
                        onDismissRequest = {
                            fishExpanded = false
                        }
                    ) {
                        // Выбрать все виды рыб
                        DropdownMenuItem(
                            text = {
                                Text("Все виды рыб")
                            },
                            onClick = {
                                selectedFishIds = emptySet()
                                fishExpanded = false
                                errorMessage = null
                            }
                        )

                        HorizontalDivider()

                        fishList.forEach { fish ->
                            val isSelected = fish.id in selectedFishIds

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(fish.name)
                                    }
                                },
                                onClick = {
                                    selectedFishIds =
                                        if (isSelected) {
                                            selectedFishIds - fish.id
                                        } else {
                                            selectedFishIds + fish.id
                                        }

                                    errorMessage = null
                                }
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text("Готово")
                            },
                            onClick = {
                                fishExpanded = false
                            }
                        )
                    }
                }

                // НАЗВАНИЕ
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Название запрета") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ОПИСАНИЕ
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = { Text("Описание") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // МЕСТО
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                    },
                    label = { Text("Место") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // НАЧАЛО ДЕЙСТВИЯ ЗАПИСИ
                OutlinedButton(
                    onClick = {
                        showValidFromDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Действует с: ${validFrom.format(dateFormatter)}"
                    )
                }

                // ОКОНЧАНИЕ ДЕЙСТВИЯ ЗАПИСИ
                OutlinedButton(
                    onClick = {
                        showValidUntilDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Действует до: ${
                            validUntil?.format(dateFormatter)
                                ?: "Бессрочно"
                        }"
                    )
                }

                if (validUntil != null) {
                    TextButton(
                        onClick = {
                            validUntil = null
                            errorMessage = null
                        }
                    ) {
                        Text("Убрать дату окончания")
                    }
                }

                HorizontalDivider()
                Text(
                    text = "Ежегодный сезон запрета",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = {
                        showSeasonStartDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Начало сезона: %02d.%02d".format(
                            seasonStartDay,
                            seasonStartMonth
                        )
                    )
                }
                OutlinedButton(
                    onClick = {
                        showSeasonEndDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Окончание сезона: %02d.%02d".format(
                            seasonEndDay,
                            seasonEndMonth
                        )
                    )
                }

                Text(
                    text = "Сезон повторяется ежегодно независимо от года.",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Период сезона, в который запрещена рыбалка.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )

    // ВЫБОР НАЧАЛА ДЕЙСТВИЯ ЗАПИСИ
    if (showValidFromDialog) {
        LocalDatePickerDialog(
            initialDate = validFrom,
            onDateSelected = { date ->
                validFrom = date
                showValidFromDialog = false
                errorMessage = null
            },
            onDismiss = {
                showValidFromDialog = false
            }
        )
    }

    // ВЫБОР ОКОНЧАНИЯ ДЕЙСТВИЯ ЗАПИСИ
    if (showValidUntilDialog) {
        LocalDatePickerDialog(
            initialDate = validUntil ?: validFrom,
            onDateSelected = { date ->
                validUntil = date
                showValidUntilDialog = false
                errorMessage = null
            },
            onDismiss = {
                showValidUntilDialog = false
            }
        )
    }

    // ВЫБОР НАЧАЛА ЕЖЕГОДНОГО СЕЗОНА
    if (showSeasonStartDialog) {
        MonthDayPickerDialog(
            initialMonth = seasonStartMonth,
            initialDay = seasonStartDay,
            title = "Начало сезона",
            onDateSelected = { month, day ->
                seasonStartMonth = month
                seasonStartDay = day

                showSeasonStartDialog = false
                errorMessage = null
            },
            onDismiss = {
                showSeasonStartDialog = false
            }
        )
    }

    // ВЫБОР ОКОНЧАНИЯ ЕЖЕГОДНОГО СЕЗОНА
    if (showSeasonEndDialog) {
        MonthDayPickerDialog(
            initialMonth = seasonEndMonth,
            initialDay = seasonEndDay,
            title = "Окончание сезона",
            onDateSelected = { month, day ->
                seasonEndMonth = month
                seasonEndDay = day

                showSeasonEndDialog = false
                errorMessage = null
            },
            onDismiss = {
                showSeasonEndDialog = false
            }
        )
    }


}

@Composable
fun MonthDayPickerDialog(
    initialMonth: Int,
    initialDay: Int,
    title: String,
    onDateSelected: (month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMonth by remember {
        mutableIntStateOf(initialMonth)
    }

    var selectedDay by remember {
        mutableIntStateOf(initialDay)
    }

    val monthNames = listOf(
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь"
    )

    val maxDay = when (selectedMonth) {
        2 -> 29
        4, 6, 9, 11 -> 30
        else -> 31
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ВЫБОР МЕСЯЦА
                Text("Месяц")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedMonth =
                                if (selectedMonth == 1) {
                                    12
                                } else {
                                    selectedMonth - 1
                                }

                            val newMaxDay = when (selectedMonth) {
                                2 -> 29
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }

                            if (selectedDay > newMaxDay) {
                                selectedDay = newMaxDay
                            }
                        }
                    ) {
                        Text("‹")
                    }

                    Text(
                        text = monthNames[selectedMonth - 1],
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = {
                            selectedMonth =
                                if (selectedMonth == 12) {
                                    1
                                } else {
                                    selectedMonth + 1
                                }

                            val newMaxDay = when (selectedMonth) {
                                2 -> 29
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }

                            if (selectedDay > newMaxDay) {
                                selectedDay = newMaxDay
                            }
                        }
                    ) {
                        Text("›")
                    }
                }

                HorizontalDivider()

                // ВЫБОР ДНЯ
                Text("День")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = selectedDay > 1,
                        onClick = {
                            selectedDay--
                        }
                    ) {
                        Text("−")
                    }

                    Text(
                        text = "%02d".format(selectedDay),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    TextButton(
                        enabled = selectedDay < maxDay,
                        onClick = {
                            selectedDay++
                        }
                    ) {
                        Text("+")
                    }
                }

                Text(
                    text = "Выбрано: %02d.%02d".format(
                        selectedDay,
                        selectedMonth
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {

                    onDateSelected(
                        selectedMonth,
                        selectedDay
                    )
                }
            ) {
                Text("Выбрать")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}


private fun isValidSeasonDate(
    month: Int,
    day: Int
): Boolean {
    return try {
        LocalDate.of(2000, month, day)
        true
    } catch (_: DateTimeException) {
        false
    }
}