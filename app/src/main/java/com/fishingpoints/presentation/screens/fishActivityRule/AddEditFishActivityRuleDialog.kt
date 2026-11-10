package com.fishingpoints.presentation.screens.fishActivityRule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFishActivityRuleDialog(
    rule: FishActivityRule?,
    fishList: List<Fish>,
    regionList: List<Region>,
    onDismiss: () -> Unit,
    onSave: (FishActivityRule) -> Unit
) {

    var selectedFish by remember(rule, fishList) {
        mutableStateOf(
            fishList.find { it.id == rule?.fishId }
        )
    }

    var selectedRegion by remember(rule, regionList) {
        mutableStateOf(
            regionList.find { it.id == rule?.regionId }
        )
    }

    var minTemperature by remember(rule) {
        mutableStateOf(rule?.temperatureMin?.toString() ?: "")
    }

    var maxTemperature by remember(rule) {
        mutableStateOf(rule?.temperatureMax?.toString() ?: "")
    }

    var description by remember(rule) {
        mutableStateOf(rule?.description ?: "")
    }

    var selectedSeason by remember(rule) {
        mutableStateOf(rule?.season)
    }

    var selectedTime by remember(rule) {
        mutableStateOf(rule?.timeOfDay)
    }

    var selectedActivity by remember(rule) {
        mutableStateOf(
            rule?.activity ?: FeedingActivity.MEDIUM
        )
    }

    var selectedConfidence by remember(rule) {
        mutableStateOf(
            rule?.confidence ?: RuleConfidence.MEDIUM
        )
    }

    var fishExpanded by remember { mutableStateOf(false) }
    var regionExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var confidenceExpanded by remember { mutableStateOf(false) }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                if (rule == null)
                    "Новое правило активности"
                else
                    "Редактирование правила"
            )
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val min = minTemperature
                        .trim()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    val max = maxTemperature
                        .trim()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    // Проверяем корректность введённых значений.

                    val temperatureError = when {

                        minTemperature.isNotBlank() && min == null ->
                            "Некорректная минимальная температура"

                        maxTemperature.isNotBlank() && max == null ->
                            "Некорректная максимальная температура"

                        else -> validateTemperature(min, max)
                    }

                    // Последовательная проверка данных.

                    when {

                        selectedFish == null -> {
                            validationError = "Выберите рыбу"
                        }

                        temperatureError != null -> {
                            validationError = temperatureError
                        }

                        else -> {

                            validationError = null

                            val newRule = FishActivityRule(

                                id = rule?.id ?: 0L,

                                fishId = selectedFish!!.id,

                                regionId = selectedRegion?.id,

                                temperatureMin = min,

                                temperatureMax = max,

                                season = selectedSeason,

                                timeOfDay = selectedTime,

                                activity = selectedActivity,

                                description = description
                                    .trim()
                                    .takeIf { it.isNotBlank() },

                                confidence = selectedConfidence,

                                source = RuleSource.CUSTOM
                            )

                            onSave(newRule)
                        }
                    }
                }
            ) {
                Text("Сохранить")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },

        text = {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp)
                    .verticalScroll(rememberScrollState()),

                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {

                // =====================================
                // РЫБА
                // =====================================

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
                            Text("Рыба *")
                        },

                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                fishExpanded
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
                                    selectedFish = fish
                                    fishExpanded = false
                                }
                            )
                        }
                    }
                }

                // =====================================
                // РЕГИОН
                // =====================================

                ExposedDropdownMenuBox(

                    expanded = regionExpanded,

                    onExpandedChange = {
                        regionExpanded = !regionExpanded
                    }

                ) {

                    OutlinedTextField(

                        value = selectedRegion?.name ?: "Все регионы",

                        onValueChange = {},

                        readOnly = true,

                        label = {
                            Text("Регион")
                        },

                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                regionExpanded
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

                        DropdownMenuItem(

                            text = {
                                Text("Все регионы")
                            },

                            onClick = {
                                selectedRegion = null
                                regionExpanded = false
                            }
                        )

                        regionList.forEach { region ->

                            DropdownMenuItem(

                                text = {
                                    Text(region.name)
                                },

                                onClick = {
                                    selectedRegion = region
                                    regionExpanded = false
                                }
                            )
                        }
                    }
                }

                // =====================================
                // ТЕМПЕРАТУРА
                // =====================================

                Text(
                    "Температура воды, °C",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(

                        value = minTemperature,

                        onValueChange = {
                            minTemperature = it
                            validationError = null
                        },

                        label = {
                            Text("От")
                        },

                        modifier = Modifier.weight(1f),

                        singleLine = true,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )

                    OutlinedTextField(

                        value = maxTemperature,

                        onValueChange = {
                            maxTemperature = it
                            validationError = null
                        },

                        label = {
                            Text("До")
                        },

                        modifier = Modifier.weight(1f),

                        singleLine = true,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                }

                Text(
                    "Пустые значения означают отсутствие ограничения.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // =====================================
                // СЕЗОН
                // =====================================

                RuleDropdown(

                    label = "Сезон",

                    value = selectedSeason?.displayName
                        ?: "Любой сезон",

                    expanded = seasonExpanded,

                    onExpandedChange = {
                        seasonExpanded = it
                    }

                ) {

                    DropdownMenuItem(

                        text = {
                            Text("Любой сезон")
                        },

                        onClick = {
                            selectedSeason = null
                            seasonExpanded = false
                        }
                    )

                    Season.entries.forEach { season ->

                        DropdownMenuItem(

                            text = {
                                Text(season.displayName)
                            },

                            onClick = {
                                selectedSeason = season
                                seasonExpanded = false
                            }
                        )
                    }
                }

                // =====================================
                // ВРЕМЯ СУТОК
                // =====================================

                RuleDropdown(

                    label = "Время суток",

                    value = selectedTime?.displayName
                        ?: "Любое время",

                    expanded = timeExpanded,

                    onExpandedChange = {
                        timeExpanded = it
                    }

                ) {

                    DropdownMenuItem(

                        text = {
                            Text("Любое время")
                        },

                        onClick = {
                            selectedTime = null
                            timeExpanded = false
                        }
                    )

                    TimeOfDay.entries.forEach { time ->

                        DropdownMenuItem(

                            text = {
                                Text(time.displayName)
                            },

                            onClick = {
                                selectedTime = time
                                timeExpanded = false
                            }
                        )
                    }
                }

                // =====================================
                // АКТИВНОСТЬ
                // =====================================

                RuleDropdown(

                    label = "Активность рыбы",

                    value = selectedActivity.displayText,

                    expanded = activityExpanded,

                    onExpandedChange = {
                        activityExpanded = it
                    }

                ) {

                    FeedingActivity.entries.forEach { activity ->

                        DropdownMenuItem(

                            text = {
                                Text(activity.displayText)
                            },

                            onClick = {
                                selectedActivity = activity
                                activityExpanded = false
                            }
                        )
                    }
                }

                // =====================================
                // ДОСТОВЕРНОСТЬ
                // =====================================

                RuleDropdown(

                    label = "Достоверность",

                    value = selectedConfidence.displayName,

                    expanded = confidenceExpanded,

                    onExpandedChange = {
                        confidenceExpanded = it
                    }

                ) {

                    RuleConfidence.entries.forEach { confidence ->

                        DropdownMenuItem(

                            text = {
                                Text(confidence.displayName)
                            },

                            onClick = {
                                selectedConfidence = confidence
                                confidenceExpanded = false
                            }
                        )
                    }
                }

                // =====================================
                // ОПИСАНИЕ
                // =====================================

                OutlinedTextField(

                    value = description,

                    onValueChange = {
                        description = it
                    },

                    label = {
                        Text("Описание правила")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    minLines = 2
                )

                // =====================================
                // ОШИБКА ВАЛИДАЦИИ
                // =====================================

                validationError?.let { error ->

                    Text(

                        text = error,

                        color = MaterialTheme.colorScheme.error,

                        style = MaterialTheme.typography.bodySmall

                    )
                }
            }
        }
    )
}



private fun validateTemperature(
    min: Double?,
    max: Double?
): String? {

    if (min != null && !min.isFinite()) {
        return "Некорректная минимальная температура"
    }

    if (max != null && !max.isFinite()) {
        return "Некорректная максимальная температура"
    }

    if (min != null && min !in -5.0..40.0) {
        return "Минимальная температура вне допустимого диапазона (-5...40 °C)"
    }

    if (max != null && max !in -5.0..40.0) {
        return "Максимальная температура вне допустимого диапазона (-5...40 °C)"
    }

    if (min != null && max != null && min > max) {
        return "Минимальная температура не может быть выше максимальной"
    }

    return null
}
