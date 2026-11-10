package com.fishingpoints.presentation.screens.fishingRule

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.RegionRule
import com.fishingpoints.presentation.formatting.formatFishingDate
import com.fishingpoints.presentation.screens.fishingRule.cards.FishLimitCard
import com.fishingpoints.presentation.screens.fishingRule.cards.FishingBanCard
import com.fishingpoints.presentation.screens.fishingRule.cards.RegionRuleCard
import com.fishingpoints.presentation.screens.fishingRule.component.RegionSelector
import com.fishingpoints.presentation.screens.fishingRule.dialogs.AddDailyAllowanceDialog
import com.fishingpoints.presentation.screens.fishingRule.dialogs.AddFishingBanDialog
import com.fishingpoints.presentation.screens.fishingRule.dialogs.AddRegionRuleDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingRuleScreen(
    drawerState: DrawerState,

    viewModel: FishingRuleViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()


    var currentDialog by remember {
        mutableStateOf<FishLimitDialog>(FishLimitDialog.None)
    }



    var pendingRegionRule by remember {
        mutableStateOf<RegionRule?>(null)
    }

    var overlappingRules by remember {
        mutableStateOf<List<RegionRule>>(emptyList())
    }

    var showOverlapWarning by remember {
        mutableStateOf(false)
    }

    var isCheckingOverlap by remember {
        mutableStateOf(false)
    }



    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("📋 Правила", "📊 Нормы", "🚫 Запреты")
    val hasRegion = state.selectedRegionId != null

    var showAllRuleHistory by rememberSaveable {
        mutableStateOf(false)
    }

    val displayedRules = if (showAllRuleHistory) {
        state.allRegionRules
    } else {
        state.regionRules
    }



    LaunchedEffect(Unit) {
        viewModel.onScreenOpened()
    }
    fun checkAndSaveRegionRule(rule: RegionRule) {
        scope.launch {
            isCheckingOverlap = true

            try {
                val overlaps = viewModel.getOverlappingRules(rule)

                pendingRegionRule = rule
                overlappingRules = overlaps

                if (overlaps.isNotEmpty()) {
                    showOverlapWarning = true
                } else {
                    if (rule.id == 0L) {
                        viewModel.insertRegionRule(rule)
                    } else {
                        viewModel.updateRegionRule(rule)
                    }

                    currentDialog = FishLimitDialog.None

                }
            } finally {
                isCheckingOverlap = false
            }
        }
    }
    // =========================
    // DIALOGS
    // =========================
    when
            (val dialog = currentDialog)
    {
        is FishLimitDialog.EditAllowance -> {
            AddDailyAllowanceDialog(
                fishList = state.fishList,
                regionList = state.regionList,
                selectedRegionId = state.selectedRegionId,
                selectedDate = state.selectedDate,
                allowance = dialog.allowance,
                onDismiss = { currentDialog = FishLimitDialog.None },
                onSave = { allowance ->
                    if (allowance.id == 0L) {
                        viewModel.insertDailyAllowance(allowance)
                    } else {
                        viewModel.updateDailyAllowance(allowance)
                    }
                    currentDialog = FishLimitDialog.None
                }
            )
        }

        is FishLimitDialog.EditBan -> {
            AddFishingBanDialog(
                fishList = state.fishList,
                regionList = state.regionList,
                selectedRegionId = state.selectedRegionId,
                selectedDate = state.selectedDate,
                ban = dialog.ban,
                onDismiss = {
                    currentDialog = FishLimitDialog.None
                },
                onSave = { ban ->
                    if (ban.id == 0L) {
                        viewModel.insertFishingBan(ban)
                    } else {
                        viewModel.updateFishingBan(ban)
                    }
                    currentDialog = FishLimitDialog.None
                }
            )

        }

        is FishLimitDialog.EditRegionRule -> {
            AddRegionRuleDialog(
                regionList = state.regionList,
                selectedRegionId = state.selectedRegionId,
                selectedDate = state.selectedDate,
                rule = dialog.rule,
                onDismiss = {
                    currentDialog = FishLimitDialog.None

                },
                onSave = { rule ->
                    checkAndSaveRegionRule(rule)
                }
            )
        }
        is FishLimitDialog.CreateRegionRuleRevision -> {

            AddRegionRuleDialog(
                regionList = state.regionList,
                selectedRegionId = dialog.rule.regionId,
                selectedDate = dialog.rule.validFrom,
                rule = dialog.rule.copy(
                    id = 0L
                ),
                onDismiss = {
                    currentDialog = FishLimitDialog.None
                },
//                onSave = { newRule ->
//
//                    scope.launch {
//
//                        try {
//                            viewModel.createRegionRuleRevision(
//                                previousRule = dialog.rule,
//                                newRule = newRule
//                            )
//
//                            currentDialog = FishLimitDialog.None
//
//                        } catch (e: Exception) {
//                            // Обработку ошибки добавим отдельно.
//                        }
//                    }
//                }
                onSave = { newRule ->

                    viewModel.createRegionRuleRevision(
                        previousRule = dialog.rule,
                        newRule = newRule
                    )

                    currentDialog = FishLimitDialog.None
                }

            )
        }





        FishLimitDialog.None -> Unit
        null -> Unit
    }

    // =========================
    // SCREEN
    // =========================
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Правила рыбалки на ${formatFishingDate(state.selectedDate)}"

                    )
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
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = "* Длину рыбы измеряют в свежем виде от вершины рыла " +
                            "при закрытом рте до основания средних лучей хвостового плавника",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },

        floatingActionButton = {
            FishLimitFab(
                selectedTab = selectedTab,
                hasRegion = hasRegion,
                onAddAllowance = {
                    currentDialog = FishLimitDialog.EditAllowance()
                },
                onAddBan = {
                    currentDialog = FishLimitDialog.EditBan()
                },
                onAddRule = {
                    currentDialog = FishLimitDialog.EditRegionRule()
                }
            )
        }


    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllRuleHistory) {
                        "История правил"
                    } else {
                        "Действующие правила"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(
                    onClick = {
                        showAllRuleHistory = !showAllRuleHistory
                    }
                ) {
                    Text(
                        if (showAllRuleHistory) {
                            "Действующие"
                        } else {
                            "Вся история"
                        }
                    )
                }
            }
            // =========================
            // SELECTORS
            // =========================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

//                Text(
//                    text = if (showAllRuleHistory) {
//                        "История правил"
//                    } else {
//                        "Действующие правила"
//                    },
//                    style = MaterialTheme.typography.titleMedium
//                )

//                TextButton(
//                    onClick = {
//                        showAllRuleHistory = !showAllRuleHistory
//                    }
//                ) {
//                    Text(
//                        if (showAllRuleHistory) {
//                            "Действующие"
//                        } else {
//                            "Вся история"
//                        }
//                    )
//                }

                RegionSelector(
                    regions = state.regionList,
                    selectedRegionId = state.selectedRegionId,
                    isDetectingRegion = state.isDetectingRegion,
                    onRegionSelected = { regionId ->
                        viewModel.selectRegion(regionId)
                    },
                    modifier = Modifier.weight(1f)
                )

                DateSelector(
                    selectedDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.selectDate(date)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            if (state.selectedDate != LocalDate.now()) {
                TextButton(
                    onClick = {
                        viewModel.showCurrentDate()
                    },
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Сегодня")
                }
            }

            // =========================
            // TABS
            // =========================
            PrimaryTabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        text = { Text(title) }
                    )
                }
            }

            state.errorMessage?.let { message ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            // =========================
            // CONTENT
            // =========================

            if (!hasRegion) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Выберите регион")
                }
            } else {
                when (selectedTab) {

                    // =========================
                    // RULE TAB
                    // =========================
                    0 -> {
                        if (displayedRules.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (showAllRuleHistory) {
                                        "История правил пока пуста"
                                    } else {
                                        "На выбранную дату правила не найдены"
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = displayedRules,
                                    key = { it.id }
                                ) { rule ->
                                    RegionRuleCard(
                                        rule = rule,
                                        selectedDate = state.selectedDate,
                                        onEdit = {
                                            currentDialog =
                                                FishLimitDialog.EditRegionRule(rule)
                                        },
                                        onCreateRevision = {
                                            currentDialog =
                                                FishLimitDialog.CreateRegionRuleRevision(rule)
                                        },
                                        onDelete = {
                                            viewModel.deleteRegionRule(rule)
                                        }
                                    )
                                }
                            }
                        }
                    }


                    // =========================
                    // ALLOWANCE
                    // =========================

                    1 -> {
                        if (state.dailyAllowance.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Нет данных")
                            }

                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                items(
                                    items = state.dailyAllowance,
                                    key = { it.id }
                                ) { allowance ->
                                    val fishName = state.fishList
                                        .find { it.id == allowance.fishId }
                                        ?.name ?: "Неизвестная рыба"


                                    FishLimitCard(
                                        allowance = allowance,
                                        name = fishName,
                                        onEdit = {
                                            currentDialog = FishLimitDialog.EditAllowance(allowance)
                                        },
                                        onDelete = { viewModel.deleteDailyAllowance(allowance) }
                                    )
                                }
                            }
                        }
                    }

                    // =========================
                    // BANS
                    // =========================

                    2 -> {
                        if (state.fishingBans.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Нет запретов")
                            }

                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                items(
                                    items = state.fishingBans,
                                    key = { it.id }
                                ) { ban ->
                                    val fishNames = state.fishList
                                        .filter { it.id in ban.fishIds }
                                        .map { it.name }

                                    FishingBanCard(
                                        ban = ban,
                                        fishNames = fishNames,
                                        onEdit = { currentDialog = FishLimitDialog.EditBan(ban) },
                                        onDelete = { viewModel.deleteFishingBan(ban) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOverlapWarning) {

        AlertDialog(
            onDismissRequest = {
                showOverlapWarning = false
                pendingRegionRule = null
            },

            title = {
                Text("Пересечение периодов")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "В выбранном периоде уже действуют следующие правила:"
                    )

                    overlappingRules.forEach { rule ->

                        Text(
                            text = "• ${rule.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Это не запрещает сохранение. Проверьте, " +
                                "не противоречат ли правила друг другу."
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        pendingRegionRule?.let { rule ->

                            if (rule.id == 0L) {
                                viewModel.insertRegionRule(rule)
                            } else {
                                viewModel.updateRegionRule(rule)
                            }
                        }

                        pendingRegionRule = null
                        showOverlapWarning = false
                        currentDialog = FishLimitDialog.None

                    }
                ) {
                    Text("Всё равно сохранить")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        pendingRegionRule = null
                        showOverlapWarning = false
                    }
                ) {
                    Text("Вернуться")
                }
            }
        )
    }





}

@Composable
private fun FishLimitFab(
    selectedTab: Int,
    hasRegion: Boolean,
    onAddAllowance: () -> Unit,
    onAddBan: () -> Unit,
    onAddRule: () -> Unit
) {
    if (!hasRegion) return

    when (selectedTab) {

        // Правила
        0 -> {
            FloatingActionButton(
                onClick = onAddRule
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить правило"
                )
            }
        }

        // Нормы
        1 -> {
            FloatingActionButton(
                onClick = onAddAllowance
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить норму"
                )
            }
        }

        // Запреты
        2 -> {
            FloatingActionButton(
                onClick = onAddBan
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить запрет"
                )
            }
        }
    }
}


sealed interface FishLimitDialog {

    data object None : FishLimitDialog

    data class EditAllowance(
        val allowance: DailyAllowance? = null
    ) : FishLimitDialog

    data class EditBan(
        val ban: FishingBan? = null
    ) : FishLimitDialog

    // Редактирование существующей редакции
    data class EditRegionRule(
        val rule: RegionRule? = null
    ) : FishLimitDialog

    // Создание новой редакции
    data class CreateRegionRuleRevision(
        val rule: RegionRule
    ) : FishLimitDialog
}


@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    OutlinedButton(
        onClick = {
            showDatePicker = true
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = formatFishingDate(selectedDate)
        )

    }


    val datePickerState = key(selectedDate) {
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
    }


    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            onDateSelected(date)
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}