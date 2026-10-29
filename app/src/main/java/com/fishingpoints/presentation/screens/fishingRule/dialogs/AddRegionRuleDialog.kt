package com.fishingpoints.presentation.screens.fishingRule.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.local.entity.RegionRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRegionRuleDialog(
    regionList: List<Region>,
    selectedRegionId: Long?,
    selectedDate: LocalDate,
    rule: RegionRule? = null,
    onDismiss: () -> Unit,
    onSave: (RegionRule) -> Unit
) {
    var selectedRegionIdLocal by remember(rule?.id, selectedRegionId) {
        mutableStateOf(rule?.regionId ?: selectedRegionId)
    }
    var validFrom by remember(rule?.id, selectedDate) {
        mutableStateOf(rule?.validFrom ?: selectedDate)
    }

    var validUntil by remember(rule?.id, selectedDate) {
        mutableStateOf(rule?.validUntil)
    }

    var dailyWeightLimit by remember {
        mutableStateOf(
            rule?.dailyWeightLimit?.toString() ?: "5000"
        )
    }

    var maxDailyNorms by remember {
        mutableStateOf(
            rule?.maxDailyNorms?.toString() ?: "2"
        )
    }

    var notes by remember {
        mutableStateOf(
            rule?.notes ?: ""
        )
    }

    var allowOversizeFish by remember {
        mutableStateOf(
            rule?.allowOversizeFish ?: true
        )
    }

    var regionExpanded by remember {
        mutableStateOf(false)
    }

    var showValidFromPicker by remember {
        mutableStateOf(false)
    }

    var showValidUntilPicker by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val selectedRegion = regionList.firstOrNull {
        it.id == selectedRegionIdLocal
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (rule == null) {
                    "Добавить правила"
                } else {
                    "Редактировать правила"
                }
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ОШИБКА
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
                            text = validFrom.format(dateFormatter),
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
                            text = validUntil?.format(dateFormatter)
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

                // СУТОЧНАЯ НОРМА
                OutlinedTextField(
                    value = dailyWeightLimit,
                    onValueChange = {
                        dailyWeightLimit = it
                        errorMessage = null
                    },
                    label = {
                        Text("Суточная норма (г)")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // МАКСИМАЛЬНОЕ КОЛИЧЕСТВО НОРМ
                OutlinedTextField(
                    value = maxDailyNorms,
                    onValueChange = {
                        maxDailyNorms = it
                        errorMessage = null
                    },
                    label = {
                        Text("Максимум суточных норм")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // РАЗРЕШЕНИЕ НА СВЕРХНОРМАТИВНУЮ РЫБУ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allowOversizeFish,
                        onCheckedChange = {
                            allowOversizeFish = it
                        }
                    )

                    Text(
                        text = "Разрешить 1 экземпляр сверх нормы"
                    )
                }

                // ПРИМЕЧАНИЯ
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                    },
                    label = {
                        Text("Примечания")
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val regionId = selectedRegionIdLocal
                    if (regionId == null || selectedRegion == null) {
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

                    val parsedWeight = dailyWeightLimit.toIntOrNull()

                    if (parsedWeight == null || parsedWeight <= 0) {
                        errorMessage =
                            "Суточная норма должна быть больше нуля"
                        return@TextButton
                    }

                    val parsedMaxNorms = maxDailyNorms.toIntOrNull()

                    if (parsedMaxNorms == null || parsedMaxNorms <= 0) {
                        errorMessage =
                            "Количество суточных норм должно быть больше нуля"
                        return@TextButton
                    }


                    val regionRule = RegionRule(
                        id = rule?.id ?: 0L,
                        regionId = regionId,
                        validFrom = validFrom,
                        validUntil = validUntil,
                        dailyWeightLimit = parsedWeight,
                        allowOversizeFish = allowOversizeFish,
                        maxDailyNorms = parsedMaxNorms,
                        notes = notes.ifBlank { null }
                    )

                    onSave(regionRule)
                }
            ) {

                Text(
                    if (rule == null)
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

// ВЫБОР ДАТЫ НАЧАЛА
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

    // ВЫБОР ДАТЫ ОКОНЧАНИЯ
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