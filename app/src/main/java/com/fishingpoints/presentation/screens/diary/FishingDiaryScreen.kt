package com.fishingpoints.presentation.screens.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.FishingEntryWithLocation
import com.fishingpoints.data.local.entity.GroupedEntries
import com.fishingpoints.presentation.screens.diary.cards.DiaryEntryCard
import com.fishingpoints.presentation.screens.diary.component.SimpleSearchBar
import com.fishingpoints.presentation.screens.diary.component.SortDropdownMenu
import com.fishingpoints.presentation.screens.diary.dialogs.AddFishingEntryDialog
import com.fishingpoints.presentation.screens.diary.dialogs.AddLocationDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FishingDiaryScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: FishingDiaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DiaryTopBar(
                showSearch = state.showSearch,
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearchClear = viewModel::clearSearch,
                onSearchOpen = viewModel::openSearch,
                onSearchClose = viewModel::closeSearch,
                onOpenDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                },

            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddDialog
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить улов"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                //  Первичная загрузка
                state.isLoading && state.entries.isEmpty() -> {
                    LoadingState()
                }

                state.errorMessage != null && state.entries.isEmpty() -> {
                    ErrorState(
                        message = state.errorMessage
                            ?: "Неизвестная ошибка",
                        onRetry = viewModel::retry
                    )
                }

                // Поиск ничего не дал
                state.filteredEntries.isEmpty() && state.searchQuery.isNotBlank() -> {
                    EmptySearchState(
                        onClearSearch = viewModel::clearSearch
                    )
                }

                // В дневнике вообще нет записей
                state.filteredEntries.isEmpty() -> {
                    EmptyFishingState(
                        onAddClick = {
                            viewModel.showAddDialog()
                        }
                    )
                }

                else -> {
                    DiaryContent(
                        filteredEntries = state.filteredEntries,
                        groupedEntries = state.groupedEntries,
                        sortOrder = state.sortOrder,
                        hasSearchQuery = state.searchQuery.isNotBlank(),
                        onSortOrderChange = viewModel::setSortOrder,
                        onEntryClick = { entryId ->
                            navController.navigate(
                                "entry_detail/$entryId"
                            )
                        },
                        onEdit = { entryId ->
                            navController.navigate(
                                "edit_entry/$entryId"
                            )
                        },
                        onDelete = viewModel::deleteEntry
                    )
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddFishingEntryDialog(
            locations = state.locations,
            newLocationId = state.newLocationId,
            onNewLocationConsumed = { viewModel.clearNewLocationId() },
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { locationId, notes, photoUri, weather ->
                viewModel.saveEntry(
                    locationId = locationId,
                    notes = notes,
                    photoUri = photoUri,
                    weather = weather
                )
            },
            onAddNewLocation = {
                viewModel.hideAddDialog()
                viewModel.showAddLocationDialog()
            }
        )
    }

    // Диалог добавления места
    if (state.showAddLocationDialog) {
        AddLocationDialog(
            onDismiss = { viewModel.hideAddLocationDialog() },
            onSave = { name, lat, lon ->
                viewModel.saveLocation(
                    name = name,
                    lat = lat,
                    lon = lon
                )
            },
            locationProvider = viewModel.locationProvider,
            geocoderService = viewModel.geocoderService
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryTopBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onOpenDrawer: () -> Unit

) {
    if (showSearch) {
        TopAppBar(
            title = {
                SimpleSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = onSearchClear,
                    onClose = onSearchClose,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            navigationIcon = {
                IconButton(
                    onClick = onSearchClose
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        )

    } else {
        TopAppBar(
            title = { Text("📔 Дневник рыбалки") },
            navigationIcon = {
                IconButton(
                    onClick = onOpenDrawer
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Меню"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onSearchOpen
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                }
            }
        )
    }
}

@Composable
private fun DiaryContent(
    filteredEntries: List<FishingEntryWithLocation>,
    groupedEntries: List<GroupedEntries>,
    sortOrder: SortOrder,
    hasSearchQuery: Boolean,
    onSortOrderChange: (SortOrder) -> Unit,
    onEntryClick: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (FishingEntry) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DiaryInfoBar(
            count = filteredEntries.size,
            hasSearchQuery = hasSearchQuery,
            sortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange
        )
        when {
            sortOrder == SortOrder.BY_REGION && !hasSearchQuery
                -> {
                GroupedDiaryList(
                    groups = groupedEntries,
                    onEntryClick = onEntryClick,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            else -> {
                DiaryList(
                    entries = filteredEntries,
                    onEntryClick = onEntryClick,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun DiaryInfoBar(
    count: Int,
    hasSearchQuery: Boolean,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (hasSearchQuery) {
                "Найдено: $count"
            } else {
                "Всего записей: $count"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SortDropdownMenu(
            currentSortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange
        )
    }
}

@Composable
private fun DiaryList(
    entries: List<FishingEntryWithLocation>,
    onEntryClick: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (FishingEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = entries,
            key = {
                it.entry.id
            }
        ) { entryWithLocation ->
            DiaryEntryCard(
                entryWithLocation = entryWithLocation,
                onClick = { onEntryClick(entryWithLocation.entry.id) },
                onEdit = { onEdit(entryWithLocation.entry.id) },
                onDelete = { onDelete(entryWithLocation.entry) }
            )
        }

        item(
            key = "bottom_spacer"
        ) {

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GroupedDiaryList(
    groups: List<GroupedEntries>,
    onEntryClick: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (FishingEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groups.forEach { group ->
            item(
                key = "region_${group.regionName}"
            ) {

                RegionHeader(
                    regionName = group.regionName,
                    count = group.entries.size
                )
            }

            items(
                items = group.entries,
                key = { "entry_${it.entry.id}" }
            ) { entryWithLocation ->
                DiaryEntryCard(
                    entryWithLocation = entryWithLocation,
                    onClick = { onEntryClick(entryWithLocation.entry.id) },
                    onEdit = { onEdit(entryWithLocation.entry.id) },
                    onDelete = { onDelete(entryWithLocation.entry) }
                )
            }
        }

        item(
            key = "bottom_spacer"
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Загрузка записей...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry
            ) {

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Повторить")
            }
        }
    }
}


// Компонент пустого состояния
@Composable
private fun EmptySearchState(
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Ничего не найдено",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Попробуйте изменить поисковый запрос",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onClearSearch
            ) {
                Text("Очистить поиск")
            }
        }
    }
}

@Composable
fun EmptyFishingState(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "📝 Нет записей о рыбалке",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Начните вести дневник своих рыболовных приключений!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Добавить первую запись")
                }
                Text(
                    text = "Или нажмите кнопку + в нижнем правом углу",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.7f
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun RegionHeader(
    regionName: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = regionName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            // Бейдж с количеством
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}