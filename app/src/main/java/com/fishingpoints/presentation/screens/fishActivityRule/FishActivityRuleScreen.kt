package com.fishingpoints.presentation.screens.fishActivityRule

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.local.enums.Season
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishActivityRuleScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: FishActivityRuleViewModel = hiltViewModel()
) {

    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val fishList by viewModel.fishList.collectAsStateWithLifecycle()
    val regionList by viewModel.regionList.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editingRule by remember {
        mutableStateOf<FishActivityRule?>(null)
    }
    var deletingRule by remember {
        mutableStateOf<FishActivityRule?>(null)
    }

    var selectedSeason by remember {
        mutableStateOf(Season.current())
    }

    var seasonExpanded by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    // =========================================
    // ФИЛЬТРАЦИЯ ПРАВИЛ
    // =========================================

    var fishSearchQuery by remember {
        mutableStateOf("")
    }
    val filteredRules = remember(
        rules,
        selectedSeason,
        fishSearchQuery,
        fishList
    ) {

        val query = fishSearchQuery.trim()

        rules.filter { rule ->

            // Фильтр по сезону
            val seasonMatches =
                selectedSeason == Season.ALL_YEAR ||
                        rule.season == selectedSeason ||
                        rule.season == Season.ALL_YEAR

            // Получаем название рыбы
            val fishName = fishList
                .firstOrNull { it.id == rule.fishId }
                ?.name ?: ""

            // Фильтр по названию рыбы
            val nameMatches =
                query.isBlank() ||
                        fishName.contains(
                            query,
                            ignoreCase = true
                        )

            seasonMatches && nameMatches
        }
    }


    // =========================================
    // ОБРАБОТКА ОШИБОК
    // =========================================

    LaunchedEffect(error) {

        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // =========================================
    // ОСНОВНОЙ ЭКРАН
    // =========================================

    Scaffold(

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        topBar = {

            TopAppBar(

                title = {
                    Text("🐟 Правила активности")
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
                            Icons.Default.Menu,
                            contentDescription = "Меню"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    editingRule = null
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить правило"
                )
            }
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {
            // ПОИСК ПО НАЗВАНИЮ РЫБЫ

            OutlinedTextField(
                value = fishSearchQuery,
                onValueChange = {
                    fishSearchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                placeholder = {
                    Text("🔍 Найти рыбу...")
                },
                label = {
                    Text("Поиск по названию")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                trailingIcon = {
                    if (fishSearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                fishSearchQuery = ""
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Очистить поиск"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )


            // =====================================
            // ПАНЕЛЬ ФИЛЬТРАЦИИ
            // =====================================

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.SpaceBetween

            ) {

                Text(
                    text = "Фильтр:  ",
                    style = MaterialTheme.typography.titleMedium
                )

                ExposedDropdownMenuBox(

                    expanded = seasonExpanded,

                    onExpandedChange = {
                        seasonExpanded = !seasonExpanded
                    },

                    modifier = Modifier.weight(1f)

                ) {


                    OutlinedTextField(

                        value = selectedSeason.displayName,

                        onValueChange = {},

                        readOnly = true,

                        singleLine = true,

                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = seasonExpanded
                            )
                        },

                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),

                        shape = RoundedCornerShape(12.dp)

                    )

                    ExposedDropdownMenu(

                        expanded = seasonExpanded,

                        onDismissRequest = {
                            seasonExpanded = false
                        }

                    ) {

                        DropdownMenuItem(

                            text = {
                                Text("🌍 Любой сезон")
                            },

                            onClick = {
                                selectedSeason = Season.ALL_YEAR
                                seasonExpanded = false
                            }
                        )

                        Season.entries
                            .filter { it != Season.ALL_YEAR }
                            .forEach { season ->

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
                }
            }

            // =====================================
            // КОЛИЧЕСТВО ПРАВИЛ
            // =====================================

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
                    text = "Найдено правил: ${filteredRules.size}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Всего: ${rules.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 4.dp
                )
            )

            // =====================================
            // СПИСОК ПРАВИЛ
            // =====================================


            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = when {
                                rules.isEmpty() ->
                                    "Правила активности пока не добавлены"

                                fishSearchQuery.isNotBlank() ->
                                    "По запросу \"$fishSearchQuery\" ничего не найдено"

                                else ->
                                    "Для выбранного сезона правил нет"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
//            }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(
                        items = filteredRules,
                        key = { it.id }
                    ) { rule ->

                        FishActivityRuleCard(
                            rule = rule,
                            fishName = viewModel.getFishName(rule.fishId),
                            regionName = viewModel.getRegionName(rule.regionId),

                            onEdit = {
                                editingRule = rule
                                showDialog = true
                            },

                            onDelete = {
                                deletingRule = rule
                            }
                        )
                    }
                }

            }
        }
    }

    // =========================================
    // ДОБАВЛЕНИЕ И РЕДАКТИРОВАНИЕ
    // =========================================

    if (showDialog) {

        AddEditFishActivityRuleDialog(

            rule = editingRule,

            fishList = fishList,

            regionList = regionList,

            onDismiss = {
                showDialog = false
            },

            onSave = { newRule ->

                viewModel.saveRule(newRule)

                showDialog = false
            }
        )
    }

    // =========================================
    // ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ
    // =========================================

    deletingRule?.let { rule ->

        AlertDialog(

            onDismissRequest = {
                deletingRule = null
            },

            title = {
                Text("Удаление правила")
            },

            text = {
                Text(
                    "Удалить правило для рыбы " +
                            "\"${viewModel.getFishName(rule.fishId)}\"?"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        viewModel.deleteRule(rule)

                        deletingRule = null
                    }

                ) {

                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deletingRule = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

