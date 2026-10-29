package com.fishingpoints.presentation.screens.fishingRule.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Region
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDailyAllowanceDialog(
    fishList: List<Fish>,
    regionList: List<Region>,
    selectedRegionId: Long?,
    selectedDate: LocalDate,
    allowance: DailyAllowance? = null,
    onDismiss: () -> Unit,
    onSave: (DailyAllowance) -> Unit
) {
    val initialFishId = allowance?.fishId
    val initialRegionId = allowance?.regionId ?: selectedRegionId

    var selectedFishId by rememberSaveable(initialFishId) {
        mutableStateOf(initialFishId)
    }

    var selectedRegionIdLocal by rememberSaveable(initialRegionId) {
        mutableStateOf(initialRegionId)
    }
    // Период действия нормы
    var validFrom by remember(allowance?.id, selectedDate) {
        mutableStateOf(allowance?.validFrom ?: selectedDate)
    }

    var validUntil by remember(allowance?.id, selectedDate) {
        mutableStateOf(allowance?.validUntil)
    }

    var minLength by remember {
        mutableStateOf(
            allowance?.minLength?.toString() ?: ""
        )
    }

    var countLimit by remember {
        mutableStateOf(
            allowance?.countLimit?.toString() ?: ""
        )
    }

    var weightLimit by remember {
        mutableStateOf(
            allowance?.weightLimit?.toString() ?: ""
        )
    }

    var fishExpanded by remember { mutableStateOf(false) }
    var regionExpanded by remember { mutableStateOf(false) }
    var showValidFromPicker by remember {
        mutableStateOf(false)
    }

    var showValidUntilPicker by remember {
        mutableStateOf(false)
    }
    var errorMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    /*
     * Получаем объекты из актуальных списков.
     *
     * Если Room загрузил данные позже — здесь автоматически
     * появятся правильные объекты.
     */
    val selectedFish =
        fishList.firstOrNull { it.id == selectedFishId }

    val selectedRegion =
        regionList.firstOrNull {
            it.id == selectedRegionIdLocal
        }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (allowance == null) {
                    "Добавить норму"
                } else {
                    "Редактировать норму"
                }
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // РЫБА
                ExposedDropdownMenuBox(
                    expanded = fishExpanded,
                    onExpandedChange = {
                        fishExpanded = !fishExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedFish?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Рыба")
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
                        fishList.forEach { fish ->
                            DropdownMenuItem(
                                text = {
                                    Text(fish.name)
                                },
                                onClick = {
                                    selectedFishId = fish.id
                                    fishExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
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
                        label = {
                            Text("Регион")
                        },
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
                                text = {
                                    Text(region.name)
                                },
                                onClick = {
                                    selectedRegionIdLocal = region.id
                                    regionExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // ДАТА НАЧАЛА ДЕЙСТВИЯ
                OutlinedButton(
                    onClick = {
                        showValidFromPicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Действует с")

                        Text(
                            validFrom.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // ДАТА ОКОНЧАНИЯ
                OutlinedButton(
                    onClick = {
                        showValidUntilPicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Действует до")

                        Text(
                            validUntil?.format(dateFormatter)
                                ?: "Без даты окончания",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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

                // МИНИМАЛЬНАЯ ДЛИНА
                OutlinedTextField(
                    value = minLength,
                    onValueChange = {
                        minLength = it
                    },
                    label = {
                        Text("Минимальная длина (см)")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ЛИМИТ КОЛИЧЕСТВА
                OutlinedTextField(
                    value = countLimit,
                    onValueChange = {
                        countLimit = it
                    },
                    label = {
                        Text("Лимит количества")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ЛИМИТ ВЕСА
                OutlinedTextField(
                    value = weightLimit,
                    onValueChange = {
                        weightLimit = it
                    },
                    label = {
                        Text("Лимит веса (г)")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val fishId = selectedFishId
                    val regionId = selectedRegionIdLocal

                    if (fishId == null) {
                        errorMessage = "Выберите рыбу"
                        return@TextButton
                    }

                    if (regionId == null) {
                        errorMessage = "Выберите регион"
                        return@TextButton
                    }

                    if (
                        validUntil != null &&
                        validUntil!!.isBefore(validFrom)
                    ) {
                        errorMessage =
                            "Дата окончания не может быть раньше даты начала"
                        return@TextButton
                    }


                    val parsedMinLength = minLength.toIntOrNull()

                    val parsedCountLimit = countLimit.toIntOrNull()

                    val parsedWeightLimit = weightLimit.toIntOrNull()

                    /*
                     * Не разрешаем отрицательные значения.
                     */
                    if (
                        parsedMinLength != null &&
                        parsedMinLength < 0
                    ) {
                        errorMessage =
                            "Введите корректную минимальную длину"
                        return@TextButton
                    }

                    if (
                        parsedCountLimit != null &&
                        parsedCountLimit < 0
                    ) {
                        errorMessage =
                            "Лимит количества не может быть отрицательным"
                        return@TextButton
                    }

                    if (
                        parsedWeightLimit != null &&
                        parsedWeightLimit < 0
                    ) {
                        errorMessage =
                            "Лимит веса не может быть отрицательным"
                        return@TextButton
                    }

                    val dailyAllowance = DailyAllowance(
                        id = allowance?.id ?: 0,
                        fishId = fishId,
                        regionId = regionId,
                        validFrom = validFrom,
                        validUntil = validUntil,
                        minLength = parsedMinLength,
                        countLimit = parsedCountLimit,
                        weightLimit = parsedWeightLimit,
                        notes = allowance?.notes
                    )

                    onSave(dailyAllowance)
                }
            ) {

                Text(
                    if (allowance == null)
                        "Сохранить"
                    else
                        "Обновить"
                )
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
    // DATE PICKER: НАЧАЛО ДЕЙСТВИЯ
    if (showValidFromPicker) {
        LocalDatePickerDialog(
            initialDate = validFrom,
            onDateSelected = { date ->
                validFrom = date
                showValidFromPicker = false
                errorMessage = null
            },
            onDismiss = {
                showValidFromPicker = false
            }
        )
    }

    // DATE PICKER: ОКОНЧАНИЕ ДЕЙСТВИЯ
    if (showValidUntilPicker) {
        LocalDatePickerDialog(
            initialDate = validUntil ?: validFrom,
            onDateSelected = { date ->
                validUntil = date
                showValidUntilPicker = false
                errorMessage = null
            },
            onDismiss = {
                showValidUntilPicker = false
            }
        )
    }
}


@Composable
fun LocalDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        onDateSelected(date)
                    }
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
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}