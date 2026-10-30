package com.fishingpoints.presentation.screens.bait

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitWithFish
import com.fishingpoints.data.local.enums.Season
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaitsScreen(
    viewModel: BaitViewModel = hiltViewModel(),
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val baits by viewModel.baits.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Состояние для выпадающего меню (теперь для каждого элемента)
    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    // Состояния для диалогов
    var showDeleteDialog by remember { mutableStateOf(false) }
    var baitToDelete by remember { mutableStateOf<Bait?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var editingBait by remember { mutableStateOf<Bait?>(null) }
    var selectedBait by remember { mutableStateOf<BaitWithFish?>(null) }
    var showBaitInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "ОК",
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Насадки и наживки")
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Открыть меню"
                        )

                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBait = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить насадку")
            }
        },

        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(
                items = baits,
                key = { it.bait.id }
            ) { baitWithFish ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    onClick = {
                        selectedBait = baitWithFish
                        showBaitInfoDialog = true
                    }

                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        // HEADER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = baitWithFish.bait.name,
                                    style = MaterialTheme.typography.titleLarge //  .titleMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                            }
                            // Выпадающее меню (три точки)
                            Box {
                                IconButton(
                                    onClick = {
                                        expandedItemId =
                                            if (expandedItemId == baitWithFish.bait.id) {
                                                null
                                            } else {
                                                baitWithFish.bait.id
                                            }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Дополнительные действия"
                                    )
                                }

                                DropdownMenu(
                                    expanded = expandedItemId == baitWithFish.bait.id,
                                    onDismissRequest = {
                                        expandedItemId = null
                                    }
                                ) {
                                    // Редактировать
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
                                            expandedItemId = null
                                            editingBait = baitWithFish.bait
                                            showDialog = true
                                        }
                                    )

                                    HorizontalDivider()

                                    // Удалить
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Удалить",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            expandedItemId = null
                                            baitToDelete = baitWithFish.bait
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }





                        }

                        // Тип и сезон
                        Text(text = "Тип - ${baitWithFish.bait.type.title}")
                        if (baitWithFish.bait.bestSeason != Season.ALL_YEAR) {
                            Text(text = "Сезон - ${baitWithFish.bait.bestSeason.title}")
                        }
                        if (baitWithFish.fish.isNotEmpty()) {
                            Text(text = "Рыбы - ${baitWithFish.fish.joinToString(", ") { it.name }}")
                        }
                    }
                }
            }
        }
    }

    //////////////// Диалог подтверждения удаления //////////////////
    if (showDeleteDialog) {
        baitToDelete?.let { bait ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    baitToDelete = null
                },
                title = { Text("Удалить запись?") },
                text = { Text("Удалить насадку(наживку) \"${bait.name}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBait(bait)
                            showDeleteDialog = false
                            baitToDelete = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            baitToDelete = null
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }

    /////////////// Диалог добавления/редактирования ///////////
    if (showDialog) {
        AddEditBaitDialog(
            bait = editingBait,
            onDismiss = {
                showDialog = false
                editingBait = null
            },
            onSave = { bait, selectedFish ->

                if (bait.id == 0L) {

                    viewModel.addBait(
                        name = bait.name,
                        type = bait.type,
                        season = bait.bestSeason,
                        selectedFish = selectedFish,
                        description = bait.description
                    )

                } else {

                    viewModel.updateBait(
                        bait = bait,
                        selectedFish = selectedFish
                    )
                }

                showDialog = false
                editingBait = null
            }

        )
    }
// Диалог информации о насадке
    if (showBaitInfoDialog) {
        selectedBait?.let { baitWithFish ->

            BaitInfoDialog(
                baitWithFish = baitWithFish,
                onDismiss = {
                    showBaitInfoDialog = false
                    selectedBait = null
                }
            )
        }
    }


}

