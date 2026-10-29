package com.fishingpoints.presentation.screens.tackle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.presentation.screens.fish.fishDetail.cards.TackleCard
import com.fishingpoints.presentation.screens.fish.listFish.FishSpeciesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacklesScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: TackleViewModel = hiltViewModel(),
    fishViewModel: FishSpeciesViewModel = hiltViewModel()
) {
    val tackles by viewModel.tackles.collectAsStateWithLifecycle()
    val fish by fishViewModel.fishList.collectAsStateWithLifecycle()
    var editingTackle by remember { mutableStateOf<Tackle?>(null) }
    var infoTackle by remember { mutableStateOf<Tackle?>(null) }
    var deleteTackle by remember { mutableStateOf<Tackle?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val selectedFishIds by remember(editingTackle?.id) {

        if (editingTackle != null) {
            viewModel.getFishIdsForTackle(
                editingTackle!!.id
            )
        } else {
            flowOf(emptySet())
        }

    }.collectAsStateWithLifecycle(
        initialValue = emptySet()
    )


    // ← НОВОЕ: Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // ← НОВОЕ: слушаем ошибки
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "ОК",
                duration = SnackbarDuration.Long
            )
        }
    }
    // Слушаем успех → закрываем диалог
    LaunchedEffect(Unit) {
        viewModel.successEvent.collect {
            showEditDialog = false
            editingTackle = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Орудия лова")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
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
                    editingTackle = null
                    showEditDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить орудие лова"
                )
            }
        },
        // ← НОВОЕ: подключаем SnackbarHost
        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) { paddingValues ->

        if (tackles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Орудия лова отсутствуют",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = tackles,
                    key = { it.id }
                ) { tackle ->
                    TackleCard(
                        tackle = tackle,
                        onClick = {
                            infoTackle = tackle
                        },
                        onEdit = {
                            editingTackle = tackle
                            showEditDialog = true
                        },
                        onDelete = {
                            deleteTackle = tackle
                        }
                    )
                }
            }
        }
    }


    // Добавление / редактирование
    if (showEditDialog) {
        AddEditTackleDialog(
            tackle = editingTackle,
            fish = fish,
            selectedFishIds = selectedFishIds,
            onDismiss = {
                showEditDialog = false
                editingTackle = null
            },
            onSave = { tackle, fishIds ->
                viewModel.saveTackle(
                    tackle = tackle,
                    fishIds = fishIds.toList()
                )
            }
        )
    }

    // Просмотр информации
    infoTackle?.let { tackle ->

        TackleInfoDialog(
            tackle = tackle,
            onDismiss = {
                infoTackle = null
            }
        )
    }

    // Подтверждение удаления
    deleteTackle?.let { tackle ->

        AlertDialog(
            onDismissRequest = { deleteTackle = null },
            title = { Text("Удаление") },
            text = { Text(text = "Удалить «${tackle.name}»?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTackle(tackle)
                        deleteTackle = null
                    }
                ) {
                    Text("Удалить")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        deleteTackle = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}