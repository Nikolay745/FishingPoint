@file:OptIn(ExperimentalMaterial3Api::class)

package com.fishingpoints.presentation.screens.fish.editFish

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fishingpoints.R
import com.fishingpoints.core.utils.saveImageToInternalStorage
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.presentation.components.MultiSelectDropdown
import com.fishingpoints.presentation.screens.fish.components.getFeedingActivityIcon
import java.io.File

@SuppressLint("MutableCollectionMutableState", "LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun FishEditScreen(
    fishId: Long,
    navController: NavController,
    viewModel: FishEditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    fun List<Season>.toggleSeason(season: Season): List<Season> =
        if (season in this) this - season else this + season

    fun List<TimeOfDay>.toggleTimeOfDay(time: TimeOfDay): List<TimeOfDay> =
        if (time in this) this - time else this + time
    // Launcher для выбора изображения
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            val savedPath = saveImageToInternalStorage(
                context = context,
                uri = uri
            )

            if (savedPath != null) {
                viewModel.updateImagePath(savedPath)
            } else {
                Toast.makeText(
                    context,
                    "Ошибка сохранения изображения",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (fishId == -1L) "Добавить рыбу" else "Редактировать")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.save {
                                navController.popBackStack()
                            }
                        },
                        enabled = state.canSave && !state.isLoading
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Поле названия
                FishNameField(
                    value = state.form.name,
                    onValueChange = viewModel::updateName
                )

                FishImageSection(
                    imagePath = state.form.imagePath,
                    fishName = state.form.name,
                    context = context,
                    onPickImage = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onDeleteImage = {
                        viewModel.updateImagePath(null)
                    }
                )


                SeasonSelectorChips(
                    selectedSeasons = state.form.seasons,
                    onSeasonToggle = { season ->
                        viewModel.updateSeasons(
                            state.form.seasons.toggleSeason(season)
                        )
                    }
                )

                TimeOfDaySelectorChips(
                    selectedTimeOfDay = state.form.timeOfDay,
                    onTimeOfDayToggle = { time ->
                        viewModel.updateTimeOfDay(
                            state.form.timeOfDay.toggleTimeOfDay(time)
                        )
                    }
                )

                // Поля ввода
                TemperatureFields(
                    min = state.form.temperatureMin,
                    max = state.form.temperatureMax,
                    onMinChange = viewModel::updateTemperatureMin,
                    onMaxChange = viewModel::updateTemperatureMax
                )

                PressureFields(
                    min = state.form.pressureMin,
                    max = state.form.pressureMax,
                    onMinChange = viewModel::updatePressureMin,
                    onMaxChange = viewModel::updatePressureMax
                )

                FeedingActivityDropdown(
                    selected = state.form.feedingActivity,
                    onSelected = viewModel::updateFeedingActivity
                )
                MultiSelectDropdown(
                    title = "Насадки / Наживки",
                    items = state.baits,
                    selectedIds = state.selectedBaitIds,
                    idSelector = { it.id },
                    nameSelector = { it.name },
                    onToggle = viewModel::toggleBait
                )

                MultiSelectDropdown(
                    title = "Снасти",
                    items = state.tackles,
                    selectedIds = state.selectedTackleIds,
                    idSelector = { it.id },
                    nameSelector = { it.name },
                    onToggle = viewModel::toggleTackle
                )

                MultiSelectDropdown(
                    title = "Прикормка",
                    items = state.feeds,
                    selectedIds = state.selectedFeedIds,
                    idSelector = { it.id },
                    nameSelector = { it.name },
                    onToggle = viewModel::toggleFeed
                )

                TextField(
                    value = state.form.description,
                    onValueChange = viewModel::updateDescription,
                    label = {
                        Text("Описание")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.form.adviceFromFishermen,
                    onValueChange = viewModel::updateAdvice,
                    label = {
                        Text("Советы рыбаков")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// Раскрывающийся список Активность клева
@Composable
fun FeedingActivityDropdown(selected: FeedingActivity, onSelected: (FeedingActivity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
//            value = getFeedingActivityIcon(selected),
            value = selected.displayText,

                    onValueChange = {},
            readOnly = true,
            label = { Text("Активность клева") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            FeedingActivity.entries.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity.displayText) },
                    onClick = {
                        onSelected(activity)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SeasonSelectorChips(
    selectedSeasons: List<Season>, //  это неизменяемый список
    onSeasonToggle: (Season) -> Unit // Функция сообщает родителю о нажатии
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Сезоны активности:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Season.SPRING to "🌱 Весна",
                Season.SUMMER to "☀️ Лето",
                Season.AUTUMN to "🍂 Осень",
                Season.WINTER to "❄️ Зима"
            ).forEach { (season, label) ->
                val isSelected = season in selectedSeasons

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // Просто сообщаем родителю, что нажали на этот сезон
                        onSeasonToggle(season)
                    },
                    label = { Text(label) },
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentWidth(),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun TimeOfDaySelectorChips(
    selectedTimeOfDay: List<TimeOfDay>,
    onTimeOfDayToggle: (TimeOfDay) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Время активности:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                TimeOfDay.MORNING to "Утро",
                TimeOfDay.DAY to "День",
                TimeOfDay.EVENING to "Вечер",
                TimeOfDay.NIGHT to "Ночь"
//                TimeOfDay.ANY to "Любое время"
            ).forEach { (timeOfDay, label) ->
                val isSelected = timeOfDay in selectedTimeOfDay
                FilterChip(
                    selected = isSelected,
                    onClick = { onTimeOfDayToggle(timeOfDay) },
                    label = { Text(label) },
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentWidth(),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun FishNameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Название рыбы") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
private fun getImageModel(
    context: Context,
    imagePath: String
): Any? {
    return when {

        imagePath.startsWith("http://") ||
                imagePath.startsWith("https://") -> {
            imagePath
        }

        imagePath.startsWith("content://") ||
                imagePath.startsWith("android.resource://") ||
                imagePath.startsWith("file://") -> {
            Uri.parse(imagePath)
        }

        // Абсолютный путь к файлу:
        // /data/user/0/com.fishingpoints/files/fish_xxx.jpg
        imagePath.startsWith("/") -> {
            File(imagePath).takeIf { it.exists() }
        }

        // Имя drawable
        else -> {
            val resourceId = context.resources.getIdentifier(
                imagePath,
                "drawable",
                context.packageName
            )

            resourceId.takeIf { it != 0 }
        }
    }
}

@Composable
fun FishImageSection(
    imagePath: String?,
    fishName: String,
    context: Context,
    onPickImage: () -> Unit,
    onDeleteImage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (!imagePath.isNullOrBlank()) {
            val imageModel = remember(imagePath) {
                getImageModel(
                    context = context,
                    imagePath = imagePath
                )
            }


            if (imageModel != null) {

                AsyncImage(
                    model = imageModel,
                    contentDescription = fishName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(
                        R.drawable.icon_fish2
                    ),
                    error = painterResource(
                        R.drawable.placeholder_image
                    )
                )

            } else {

                Text(
                    text = "Не удалось загрузить изображение",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImage
                ) {
                    Text("Заменить")
                }

                OutlinedButton(
                    onClick = onDeleteImage
                ) {
                    Text("Удалить")
                }
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        onPickImage()
                    },
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Добавить изображение",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}



@Composable
fun TemperatureFields(
    min: Int?,
    max: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit
) {
    NumericRangeFields(
        title = "Температура (°C):",
        min = min,
        max = max,
        onMinChange = onMinChange,
        onMaxChange = onMaxChange
    )
}

@Composable
fun PressureFields(
    min: Int?,
    max: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit
) {
    NumericRangeFields(
        title = "Давление (мм рт. ст.):",
        min = min,
        max = max,
        onMinChange = onMinChange,
        onMaxChange = onMaxChange
    )
}



@Composable
fun NumericRangeFields(
    title: String,
    min: Int?,
    max: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = min?.toString().orEmpty(),
                onValueChange = { onMinChange(it.toIntOrNull()) },
                label = { Text("Мин") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = max?.toString().orEmpty(),
                onValueChange = { onMaxChange(it.toIntOrNull()) },
                label = { Text("Макс") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
