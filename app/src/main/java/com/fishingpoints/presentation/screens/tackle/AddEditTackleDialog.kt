package com.fishingpoints.presentation.screens.tackle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.enums.TackleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTackleDialog(
    tackle: Tackle?,
    fish: List<Fish>,
    selectedFishIds: Set<Long>,
    onDismiss: () -> Unit,
    onSave: (Tackle, Set<Long>) -> Unit
) {
    var name by remember(tackle) {
        mutableStateOf(tackle?.name ?: "")
    }

    var type by remember(tackle) {
        mutableStateOf(tackle?.type ?: TackleType.ROD)
    }

    var description by remember(tackle) {
        mutableStateOf(tackle?.description ?: "")
    }

    var typeExpanded by remember {
        mutableStateOf(false)
    }

    var nameError by remember(tackle) {
        mutableStateOf(false)
    }

    var selectedFish by remember(tackle, selectedFishIds) {
        mutableStateOf(selectedFishIds)
    }

    var showFishDialog by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@TextButton
                    }

                    onSave(
                        Tackle(
                            id = tackle?.id ?: 0L,
                            name = name.trim(),
                            type = type,
                            description = description.trim()
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
                text = if (tackle == null) {
                    "Добавить орудие лова"
                } else {
                    "Редактировать орудие лова"
                }
            )
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // -----------------------------------------
                // Название
                // -----------------------------------------

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = {
                        Text("Название орудия лова")
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Введите название")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // -----------------------------------------
                // Тип
                // -----------------------------------------

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = {
                        typeExpanded = !typeExpanded
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = type.title,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Тип орудия лова")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = typeExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            )
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = {
                            typeExpanded = false
                        }
                    ) {

                        TackleType.entries.forEach { tackleType ->

                            DropdownMenuItem(
                                text = {
                                    Text(tackleType.title)
                                },
                                onClick = {
                                    type = tackleType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // -----------------------------------------
                // Описание
                // -----------------------------------------

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text("Описание орудия лова")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                HorizontalDivider()

                // -----------------------------------------
                // Рыбы
                // -----------------------------------------

                OutlinedButton(
                    onClick = {
                        showFishDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedFish.isEmpty()) {
                            "Выбрать рыбу"
                        } else {
                            "Выбрано рыб: ${selectedFish.size}"
                        }
                    )
                }

                // Выбранные рыбы

                if (selectedFish.isNotEmpty()) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = "Выбранные рыбы",
                            style = MaterialTheme.typography.labelLarge
                        )

                        fish
                            .filter { it.id in selectedFish }
                            .forEach { item ->

                                AssistChip(
                                    onClick = {
                                        selectedFish =
                                            selectedFish - item.id
                                    },
                                    label = {
                                        Text(item.name)
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Удалить"
                                        )
                                    }
                                )
                            }
                    }
                }
            }
        }
    )

    // -----------------------------------------
    // Диалог выбора рыб
    // -----------------------------------------

    if (showFishDialog) {

        FishSelectionDialog(
            fish = fish,
            selectedFishIds = selectedFish,
            onDismiss = {
                showFishDialog = false
            },
            onSelectionChanged = {
                selectedFish = it
            }
        )
    }
}
