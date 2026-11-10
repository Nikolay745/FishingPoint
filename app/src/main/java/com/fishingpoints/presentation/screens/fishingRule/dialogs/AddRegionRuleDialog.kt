package com.fishingpoints.presentation.screens.fishingRule.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.local.entity.RegionRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
    val localDateSaver = Saver<LocalDate, String>(
        save = { it.toString() },
        restore = { LocalDate.parse(it) }
    )

    var selectedRegionIdLocal by rememberSaveable(
        rule?.id,
        selectedRegionId
    ) {
        mutableStateOf(rule?.regionId ?: selectedRegionId)
    }

    var title by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.title ?: "")
    }

    var description by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.description ?: "")
    }

    var notes by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.notes ?: "")
    }

    var validFrom by rememberSaveable(
        rule?.id,
        selectedDate,
        stateSaver = localDateSaver
    ) {
        mutableStateOf(rule?.validFrom ?: selectedDate)
    }

    val nullableDateSaver = Saver<LocalDate?, String>(
        save = { date ->
            date?.toString() ?: ""
        },
        restore = { value ->
            value.takeIf { it.isNotEmpty() }?.let(LocalDate::parse)
        }
    )


    var validUntil by rememberSaveable(
        rule?.id,
        selectedDate,
        stateSaver = nullableDateSaver
    ) {
        mutableStateOf(rule?.validUntil)
    }

    var regionExpanded by remember {
        mutableStateOf(false)
    }

    var showValidFromDialog by remember {
        mutableStateOf(false)
    }

    var showValidUntilDialog by remember {
        mutableStateOf(false)
    }

    var errorMessage by rememberSaveable(rule?.id) {
        mutableStateOf<String?>(null)
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    val selectedRegion = regionList.firstOrNull {
        it.id == selectedRegionIdLocal
    }

    var dailyWeightLimit by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.dailyWeightLimit?.toString() ?: "")
    }

    var allowOversizeFish by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.allowOversizeFish)
    }

    var maxDailyNorms by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.maxDailyNorms?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                if (rule == null) {
                    "Добавить региональное правило"
                } else {
                    "Редактировать правило"
                }
            )
        },

        confirmButton = {
            TextButton(
                onClick = {

                    val regionId = selectedRegionIdLocal

                    if (regionId == null || selectedRegion == null) {
                        errorMessage = "Выберите регион"
                        return@TextButton
                    }

                    if (title.isBlank()) {
                        errorMessage = "Введите название правила"
                        return@TextButton
                    }

                    if (description.isBlank()) {
                        errorMessage = "Введите содержание правила"
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

                    val regionRule = RegionRule(
                        id = rule?.id ?: 0L,
                        regionId = regionId,
                        title = title.trim(),
                        description = description.trim(),
                        validFrom = validFrom,
                        validUntil = validUntil,
                        dailyWeightLimit = dailyWeightLimit.toIntOrNull(),
                        allowOversizeFish = allowOversizeFish,
                        maxDailyNorms = maxDailyNorms.toIntOrNull(),
                        notes = notes.trim().ifBlank { null }
                    )


                    errorMessage = null

                    onSave(regionRule)
                }
            ) {
                Text(
                    if (rule == null) "Сохранить" else "Обновить"
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
                modifier = Modifier.verticalScroll(rememberScrollState()),
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

                // НАЗВАНИЕ

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = {
                        Text("Название правила")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // СОДЕРЖАНИЕ

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text("Содержание правила")
                    },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // ДОПОЛНИТЕЛЬНЫЕ ПРИМЕЧАНИЯ

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                    },
                    label = {
                        Text("Примечания")
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()

                Text(
                    text = "Дополнительные ограничения",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = dailyWeightLimit,
                    onValueChange = {
                        dailyWeightLimit = it.filter(Char::isDigit)
                    },
                    label = {
                        Text("Суточное ограничение веса (г)")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxDailyNorms,
                    onValueChange = {
                        maxDailyNorms = it.filter(Char::isDigit)
                    },
                    label = {
                        Text("Количество суточных норм")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Разрешение на вылов сверх установленного размера")

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = allowOversizeFish == true,
                        onClick = {
                            allowOversizeFish = true
                        }
                    )
                    Text("Разрешён")

                    Spacer(Modifier.width(12.dp))

                    RadioButton(
                        selected = allowOversizeFish == false,
                        onClick = {
                            allowOversizeFish = false
                        }
                    )
                    Text("Запрещён")
                }

                TextButton(
                    onClick = {
                        allowOversizeFish = null
                    }
                ) {
                    Text("Не указано")
                }

                HorizontalDivider()

                Text(
                    text = "Период действия",
                    style = MaterialTheme.typography.titleMedium
                )

                // НАЧАЛО ДЕЙСТВИЯ

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

                // ОКОНЧАНИЕ ДЕЙСТВИЯ

                OutlinedButton(
                    onClick = {
                        showValidUntilDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Действует до: ${
                            validUntil?.format(dateFormatter) ?: "Бессрочно"
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
            }
        }
    )

    // ВЫБОР ДАТЫ НАЧАЛА

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

    // ВЫБОР ДАТЫ ОКОНЧАНИЯ

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
}
