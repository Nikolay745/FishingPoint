package com.fishingpoints.presentation.screens.fishingRule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.EquipmentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    navController: NavController,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Диалог добавления / редактирования
    var showEditDialog by remember { mutableStateOf(false) }

    var editingEquipment by remember { mutableStateOf<EquipmentEntity?>(null) }

    var equipmentName by remember { mutableStateOf("") }

    // Диалог удаления
    var equipmentToDelete by remember { mutableStateOf<EquipmentEntity?>(null) }

    // Диалог сброса отметок
    var showResetDialog by remember { mutableStateOf(false) }

    var showPackedOnly by rememberSaveable { mutableStateOf(false) }
    // списки предметов
    val visibleEquipment = remember(
        state.equipment,
        showPackedOnly
    ) {
        state.equipment.filter { equipment ->
            equipment.isPacked == showPackedOnly
        }
    }

    // Показываем ошибки Repository / ViewModel
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // =========================
    // ДИАЛОГ ДОБАВЛЕНИЯ / РЕДАКТИРОВАНИЯ
    // =========================

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingEquipment = null
                equipmentName = ""
            },
            title = {
                Text(
                    text = if (editingEquipment == null) {
                        "Добавить предмет"
                    } else {
                        "Редактировать предмет"
                    }
                )
            },
            text = {
                OutlinedTextField(
                    value = equipmentName,
                    onValueChange = {
                        equipmentName = it
                    },
                    label = {
                        Text("Название предмета")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = equipmentName.isNotBlank(),
                    onClick = {
                        val name = equipmentName.trim()
                        val equipment = editingEquipment

                        if (equipment == null) {
                            viewModel.addEquipment(name)
                        } else {
                            viewModel.updateEquipment(
                                equipment = equipment,
                                newName = name
                            )
                        }

                        showEditDialog = false
                        editingEquipment = null
                        equipmentName = ""
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingEquipment = null
                        equipmentName = ""
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // =========================
    // ДИАЛОГ УДАЛЕНИЯ
    // =========================

    equipmentToDelete?.let { equipment ->

        AlertDialog(
            onDismissRequest = {
                equipmentToDelete = null
            },
            title = {
                Text("Удалить предмет?")
            },
            text = {
                Text(
                    "Удалить «${equipment.name}» из списка экипировки?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEquipment(equipment)
                        equipmentToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        equipmentToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // =========================
    // ДИАЛОГ СБРОСА ОТМЕТОК
    // =========================

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
            },
            title = {
                Text("Сбросить отметки?")
            },
            text = {
                Text(
                    "Все отметки будут сняты. " +
                            "Список экипировки останется без изменений."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllEquipment()
                        showResetDialog = false
                    }
                ) {
                    Text("Сбросить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // =========================
    // ОСНОВНОЙ ЭКРАН
    // =========================

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Экипировка")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showPackedOnly = !showPackedOnly
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (showPackedOnly) {
                            "← К списку несобранных"
                        } else {
                            "✓ Собрано (${state.packedCount})"
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))
                when {
                    state.totalCount == 0 -> {
                        Text(
                            text = "Добавьте необходимые вещи",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.packedCount == state.totalCount -> {
                        Text(
                            text = "🎣 Можно на рыбалку!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    else -> {
                        Text(
                            text = "Осталось собрать: ${state.totalCount - state.packedCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showResetDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Сбросить")
                    }

                    Button(
                        onClick = {
                            editingEquipment = null
                            equipmentName = ""
                            showEditDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Добавить")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }


    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Text(
                text = "Минимальный набор",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Отметьте вещи, которые уже взяли с собой.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (visibleEquipment.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            state.equipment.isEmpty() ->
                                "Список пуст. Добавьте необходимые предметы."

                            showPackedOnly ->
                                "Пока ничего не собрано"

                            else ->
                                "🎣 Всё собрано! Можно на рыбалку!"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = visibleEquipment,
                        key = { equipment -> equipment.id }
                    ) { equipment ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = equipment.isPacked,
                                onCheckedChange = { checked ->
                                    viewModel.toggleEquipment(
                                        equipment = equipment,
                                        isPacked = checked
                                    )
                                }
                            )

                            Text(
                                text = equipment.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.toggleEquipment(
                                            equipment = equipment,
                                            isPacked = !equipment.isPacked
                                        )
                                    },
                                style = MaterialTheme.typography.bodyLarge
                            )

                            var expanded by remember(equipment.id) {
                                mutableStateOf(false)
                            }

                            Box {
                                IconButton(
                                    onClick = {
                                        expanded = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Действия с ${equipment.name}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = {
                                        expanded = false
                                    }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Редактировать")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            expanded = false
                                            editingEquipment = equipment
                                            equipmentName = equipment.name
                                            showEditDialog = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text("Удалить")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            expanded = false
                                            equipmentToDelete = equipment
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BoxEmptyEquipment() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Список пуст. Добавьте необходимые предметы.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

