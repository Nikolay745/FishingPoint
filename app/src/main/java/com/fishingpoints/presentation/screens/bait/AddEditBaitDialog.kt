package com.fishingpoints.presentation.screens.bait

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.BaitType
import com.fishingpoints.data.local.enums.Season

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBaitDialog(
    bait: Bait?,
    onDismiss: () -> Unit,
    onSave: (Bait, List<Fish>) -> Unit,
    viewModel: BaitViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(bait?.name ?: "") }
    var description by remember { mutableStateOf(bait?.description ?: "") }
    var type by remember { mutableStateOf(bait?.type ?: BaitType.NATURAL) }
    var season by remember { mutableStateOf(bait?.bestSeason ?: Season.SUMMER) }

    // Состояние для выбранных рыб
    var selectedFish by remember { mutableStateOf<List<Fish>>(emptyList()) }
    var fishSearchText by remember { mutableStateOf("") }
    var isFishDropdownExpanded by remember { mutableStateOf(false) }

    // Получаем список всех рыб из базы
    val allFish by viewModel.allFish.collectAsState(initial = emptyList())

    // Фильтруем рыб по поисковому запросу
    val filteredFish = remember(allFish, fishSearchText) {
        if (fishSearchText.isBlank()) {
            allFish
        } else {
            allFish.filter {
                it.name.contains(fishSearchText, ignoreCase = true)
            }
        }
    }
// При редактировании загружаем выбранные рыбы
    LaunchedEffect(bait, allFish) {
        bait?.let {
            if (allFish.isNotEmpty()) {
                val fishIds = viewModel.getFishIdsForBait(it.id)
                val fishList = allFish.filter { fish -> fish.id in fishIds }
                selectedFish = fishList
            }
        }
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        Bait(
                            id = bait?.id ?: 0,
                            name = name,
                            type = type,
                            bestSeason = season,
                            description = description
                        ),
                        selectedFish
                    )
                }
            ) {
                Text("Сохранить")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        },

        title = {
            Text(
                if (bait == null)
                    "Добавить наживку"
                else
                    "Редактировать"
            )
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )  {
                // Название
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank()
                )
                if (name.isBlank()) {
                    Text(
                        text = "Название обязательно",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Поле для описания
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                 // Выбор рыб (выпадающий список)

                Text(
                    text = "Для каких рыб",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Поле поиска с выпадающим списком
                ExposedDropdownMenuBox(
                    expanded = isFishDropdownExpanded,
                    onExpandedChange = { isFishDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fishSearchText,
                        onValueChange = {
                            fishSearchText = it
                            isFishDropdownExpanded = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Поиск рыб...") },
                        placeholder = { Text("Начните вводить название рыбы") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFishDropdownExpanded)
                        },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = isFishDropdownExpanded,
                        onDismissRequest = { isFishDropdownExpanded = false }
                    ) {
                        if (filteredFish.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Рыбы не найдены",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { }
                            )
                        } else {
                            filteredFish.forEach { fish ->
                                val isSelected = selectedFish.any { it.id == fish.id }
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = fish.name,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (isSelected) {
                                            selectedFish = selectedFish.filter { it.id != fish.id }
                                        } else {
                                            selectedFish = selectedFish + fish
                                        }
                                        fishSearchText = ""
                                    }
                                )
                            }
                        }
                    }
                }
                // Отображение выбранных рыб
                if (selectedFish.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedFish.forEach { fish ->
                            AssistChip(
                                onClick = {
                                    selectedFish = selectedFish.filter { it.id != fish.id }
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fish.name)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }




                Spacer(modifier = Modifier.height(8.dp))

                // Выбор типа
                Text(
                    text = "Тип наживки",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                DropdownMenuBox(
                    options = BaitType.entries,
                    selected = type,
                    onSelected = { type = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Выбор сезона
                Text(
                    text = "Лучший сезон",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                DropdownMenuBox(
                    options = Season.entries,
                    selected = season,
                    onSelected = { season = it }
                )
            }
        }
    )
}

@Composable
fun <T> DropdownMenuBox(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Text(
                when (selected) {
                    is BaitType -> selected.title
                    is Season -> selected.title
                    else -> selected.toString()
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (item) {
                                is BaitType -> item.title
                                is Season -> item.title
                                else -> item.toString()
                            }
                        )
                    },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}