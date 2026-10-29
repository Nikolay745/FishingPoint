package com.fishingpoints.presentation.screens.feed

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val feeds by viewModel.feeds.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingFeed by remember { mutableStateOf<Feed?>(null) }

    // Состояние для выпадающего меню (теперь для каждого элемента)
    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    // Состояния для диалогов
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFeedInfoDialog by remember { mutableStateOf(false) }
    var feedToDelete by remember { mutableStateOf<Feed?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (uiState.error != null) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = uiState.error!!,
                actionLabel = "Закрыть",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Приманки(прикормки)")
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingFeed = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(feeds) { feed ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    onClick = {
                        editingFeed = feed
                        showFeedInfoDialog = true
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
                                    text = feed.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                            }
                            // Выпадающее меню (три точки)
                            Box {
                                IconButton(
                                    onClick = {
                                        expandedItemId =
                                            if (expandedItemId == feed.id) null else feed.id
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = expandedItemId == feed.id,
                                    onDismissRequest = {
                                        expandedItemId = null
                                    }
                                ) {
                                    // Пункт "Редактировать"
                                    DropdownMenuItem(
                                        text = { Text("Редактировать") },
                                        onClick = {
                                            expandedItemId = null
                                            editingFeed = feed
                                            showDialog = true
                                        }
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        DividerDefaults.color
                                    )

                                    // Пункт "Удалить"
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Удалить",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            expandedItemId = null
                                            feedToDelete = feed
                                            showDeleteDialog = true
                                        }
                                    )

                                }
                            }
                        }
                        // Описание (если есть)
                        if (!feed.description.isNullOrEmpty()) {
                            val fishs = feed.bestForFish
                            Text(
                                text = "Для рыб: $fishs", //    .description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        //  сезон
                        val season = feed.bestSeason.title
                        Text(
                            text = "Сезоны:  $season", //    .description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
            }
        }
    }

    //////////////// Диалог подтверждения удаления //////////////////
    if (showDeleteDialog) {
        feedToDelete?.let { feed ->
            AlertDialog(
                onDismissRequest = { feedToDelete = null },
                title = { Text("Удалить запись?") },
                text = { Text("Удалить насадку(наживку) \"${feed.name}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteFeed(feed)
                            showDeleteDialog = false
                            feedToDelete = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            feedToDelete = null
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
        AddEditFeedDialog(
            feed = editingFeed,
            onDismiss = {
                showDialog = false
                editingFeed = null
            },
            onSave = { feed ->
                if (feed.id == 0L) {
                    viewModel.addFeed(
                        name = feed.name,
                        season = feed.bestSeason,
                        fishNames = feed.bestForFish,
                        description = feed.description
                    )
                } else {

                    // Обновляем прикормку
                    viewModel.updateFeed(
                        feed = feed.copy(
                            bestForFish = feed.bestForFish
                        ),
                        fishNames = feed.bestForFish
                    )
                }
                showDialog = false
                editingFeed = null
            }
        )
    }
// Диалог информации о прикормке
    if (showFeedInfoDialog) {
        FeedInfoDialog(
            feed = editingFeed,
            onDismiss = {
                showFeedInfoDialog = false
                editingFeed = null
            },
        )
    }

}
