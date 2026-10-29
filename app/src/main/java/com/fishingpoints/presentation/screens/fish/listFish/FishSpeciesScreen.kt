package com.fishingpoints.presentation.screens.fish.listFish

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.presentation.screens.fish.listFish.cards.SwipeFishCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishSpeciesScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: FishSpeciesViewModel = hiltViewModel()
) {
    val fishList by viewModel.fishList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSeason by viewModel.selectedSeason.collectAsStateWithLifecycle()
    val selectedTimeOfDay by viewModel.selectedTimeOfDay.collectAsStateWithLifecycle()

    var deletingFish by remember { mutableStateOf<Fish?>(null) }
    var showSeasonSheet by remember { mutableStateOf(false) }
    var showTimeOfDaySheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("fish_edit?fishId=-1")
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("🐟 Виды рыб") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Меню")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // =========================
            // ФИЛЬТРЫ
            // =========================
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Фильтр по сезону
                OutlinedButton(
                    onClick = { showSeasonSheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                ) {
                    Text(selectedSeason?.let { getSeasonIcon(it) } ?: "🌍 Фильтр: Сезоны")
                }

                // Время суток
                OutlinedButton(
                    onClick = { showTimeOfDaySheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        selectedTimeOfDay?.let { getTimeOfDayIcon(it) }
                            ?: "🕐 Время"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Поиск

            FishSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )


            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp,
                    vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = fishList,
                    key = { it.id }
                ) { fish ->

                    SwipeFishCard(
                        fish = fish,
                        onClick = {
                            navController.navigate(
                                "fish_detail/${fish.id}"
                            )
                        },
                        onDelete = {
                            deletingFish = fish
                        }
                    )
                }
            }
        }
    }

    // ДИАЛОГ УДАЛЕНИЯ
    deletingFish?.let { fish ->

        AlertDialog(
            onDismissRequest = {
                deletingFish = null
            },
            title = { Text("Удаление") },
            text = { Text("Удалить рыбу \"${fish.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fishToDelete = fish
                        deletingFish = null

                        scope.launch {
                            try {
                                val deletedFish = viewModel.deleteFish(fishToDelete)

                                val result = snackbarHostState.showSnackbar(
                                    message = "Рыба удалена",
                                    actionLabel = "Отменить",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDeleteFish(deletedFish)
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Log.e(
                                    "FishDelete",
                                    "Ошибка при удалении рыбы",
                                    e
                                )

                                snackbarHostState.showSnackbar(
                                    message = "Не удалось удалить рыбу"
                                )
                            }
                        }
                    }
                ) {
                    Text("Удалить")
                }
            } ,

            dismissButton = {
                TextButton(
                    onClick = {
                        deletingFish = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showSeasonSheet) {
        SeasonFilterBottomSheet(
            selectedSeason = selectedSeason,
            onSelected = {
                viewModel.updateSelectedSeason(it)
            },
            onDismiss = {
                showSeasonSheet = false
            }
        )
    }

    if (showTimeOfDaySheet) {
        TimeOfDayFilterBottomSheet(
            selectedTimeOfDay = selectedTimeOfDay,
            onSelected = {
                viewModel.updateSelectedTimeOfDay(it)
            },
            onDismiss = {
                showTimeOfDaySheet = false
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonFilterBottomSheet(
    selectedSeason: Season?,
    onSelected: (Season?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Text(
                text = "Выберите сезон",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            FilterItem(
                title = "🌍 Все сезоны",
                selected = selectedSeason == null,
                onClick = {
                    onSelected(null)
                    onDismiss()
                }
            )

            Season.entries.forEach { season ->
                FilterItem(
                    title = season.displayName,
                    selected = selectedSeason == season,
                    onClick = {
                        onSelected(season)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeOfDayFilterBottomSheet(
    selectedTimeOfDay: TimeOfDay?,
    onSelected: (TimeOfDay?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Text(
                text = "Выберите время активности",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            FilterItem(
                title = "🕐 Любое время",
                selected = selectedTimeOfDay == null,
                onClick = {
                    onSelected(null)
                    onDismiss()
                }
            )

            TimeOfDay.entries.forEach { time ->
                FilterItem(
                    title = time.displayName,
                    selected = selectedTimeOfDay == time,
                    onClick = {
                        onSelected(time)
                        onDismiss()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}


@Composable
fun FilterItem(title: String, selected: Boolean, onClick: () -> Unit) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FishSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text("🔍 Найти рыбу...")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Очистить"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

fun getTimeOfDayIcon(timeOfDay: TimeOfDay?): String =
    timeOfDay?.displayName ?: "⏰ Не указано"

fun getSeasonIcon(season: Season?): String =
    season?.displayName ?: "🌍 Фильтр: сезоны"