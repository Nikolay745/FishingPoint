package com.fishingpoints.presentation.screens.diary

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fishingpoints.core.utils.FileUtil
import com.fishingpoints.presentation.formatting.formatFishingDate
import com.fishingpoints.presentation.formatting.formatFishingDateTime
import com.fishingpoints.presentation.formatting.formatFishingTime
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.LocationEntity
import java.io.File
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    navController: NavController,
    entryId: Long,
    viewModel: EditEntryViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val allLocations by viewModel.allLocations.collectAsStateWithLifecycle()

    // Состояния для диалогов
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Загружаем запись при первом запуске
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
        viewModel.loadAllLocations()
    }

    // Обработка успешного сохранения
    LaunchedEffect(isSaved) {
        if (isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✏️ Редактирование записи") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить запись",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Загрузка записи...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Ошибка",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorMessage ?: "Неизвестная ошибка",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.loadEntry(entryId) }
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                entry != null -> {
                    EditEntryContent(
                        entry = entry!!,
                        currentLocationName = locationName ?: "Место не найдено",
                        locations = allLocations,

                        onUpdate = { locationId, weather, notes, photoUri, dateEnd ->
                            viewModel.updateEntry(
                                locationId = locationId,
                                weather = weather,
                                notes = notes,
                                photoUri = photoUri,
                                dateEnd = dateEnd
                            )
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )

                }
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = {
                Text("Вы уверены, что хотите удалить эту запись? Это действие нельзя отменить.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryContent(
    entry: FishingEntry,
    currentLocationName: String,
    locations: List<LocationEntity>,
    onUpdate: (
        locationId: Long,
        weather: String?,
        notes: String?,
        photoUri: String?,
        dateEnd: Date?
    ) -> Unit,
    onCancel: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }
    var notes by rememberSaveable { mutableStateOf(entry.notes ?: "") }
    var weather by rememberSaveable { mutableStateOf(entry.weather ?: "") }
    var weatherBeforeEdit by rememberSaveable { mutableStateOf("") }

    var originalPhotoUri by rememberSaveable { mutableStateOf(entry.photoUri) }
    var photoUri by rememberSaveable { mutableStateOf(entry.photoUri) }

    var dateEnd by rememberSaveable { mutableStateOf(entry.dateEnd?.time) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var showLocationList by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showWeatherDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Инициализация selectedLocation при загрузке
    LaunchedEffect(locations) {
        if (locations.isNotEmpty() && selectedLocation == null) {
            val currentLocation = locations.find { it.id == entry.locationId }
            selectedLocation = currentLocation
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        val newPhotoPath =
            FileUtil.copyToInternalStorage(
                context = context,
                sourceUri = uri
            )

        if (newPhotoPath != null) {
            // Если ранее пользователь уже выбрал другую новую фотографию,
            // удаляем её, потому что она больше не нужна.
            if (
                photoUri != originalPhotoUri &&
                photoUri != null
            ) {
                FileUtil.deleteFile(photoUri)
            }

            photoUri = newPhotoPath
        }
    }


    // Диалог выбора места
    if (showLocationList) {
        Dialog(onDismissRequest = { showLocationList = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Выберите место рыбалки",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Поиск места") },
                        placeholder = { Text("Введите название места") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Список мест с прокруткой
                    val filteredLocations = remember(searchQuery) {
                        if (searchQuery.isEmpty()) {
                            locations
                        } else {
                            locations.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredLocations.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Места не найдены",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Здесь список мест
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredLocations) { location ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLocation = location
                                            showLocationList = false
                                            searchQuery = ""
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedLocation?.id == location.id)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = location.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "📍 ${String.format("%.4f", location.lat)}, " +
                                                    String.format("%.4f", location.lon),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог погоды
    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            title = { Text("🌤 Погода") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weather,
                        onValueChange = { weather = it },
                        label = { Text("Погодные условия") },
                        placeholder = { Text("Например: ☀️ Солнечно, +25°C") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Text(
                        text = "Быстрые шаблоны:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weatherTemplates = listOf(
                            "☀️ Солнечно, 25°C",
                            "⛅ Облачно, 18°C",
                            "🌧 Дождь, 12°C",
                            "🌤 Переменная облачность, 20°C",
                            "💨 Ветрено, 15°C"
                        )

                        items(weatherTemplates) { template ->
                            FilterChip(
                                selected = false,
                                onClick = { weather = template },
                                label = { Text(template) },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWeatherDialog = false
                    }) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeatherDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Основной контент
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Информация о записи (дата, ID)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "📅 ${formatFishingDate(entry.date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "🕐 Начало: ${formatFishingTime(entry.date)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "🏁 Окончание: ${
                        dateEnd?.let { formatFishingTime(Date(it)) } ?: "не указано"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dateEnd != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "ID записи: #${entry.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showEndDatePicker = true
                },
            colors = CardDefaults.cardColors(
                containerColor = if (dateEnd != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏁 Окончание рыбалки",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = dateEnd?.let {
                            formatFishingDateTime(Date(it))
                        } ?: "Добавить дату и время окончания",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Изменить окончание"
                )
            }
        }

        OutlinedButton(
            onClick = {
                showEndTimePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = dateEnd?.let {
                    "Изменить время окончания (${formatFishingTime(Date(it))})"
                } ?: "Добавить время окончания"
            )
        }


        // Текущее место
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLocationList = true },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedLocation != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📍 Место рыбалки",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = selectedLocation?.name ?: currentLocationName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Выбрать место"
                )
            }
        }

        // Погода
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    weatherBeforeEdit = weather
                    showWeatherDialog = true
                },
            colors = CardDefaults.cardColors(
                containerColor = if (weather.isNotEmpty())
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🌤 Погода",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = weather.ifEmpty { "Добавить погоду" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (weather.isNotEmpty())
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать погоду",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Фото
        if (!photoUri.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(photoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Фото улова",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Кнопка удаления фото
                IconButton(
                    onClick = { photoUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Удалить фото",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Кнопка изменения фото
        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                if (photoUri == null) Icons.Default.PhotoCamera else Icons.Default.Photo,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (photoUri == null) "Добавить фото улова" else "Изменить фото")
        }

        // Заметки
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("📝 Заметки") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            minLines = 5,
            maxLines = 10
        )

        // Кнопка сохранения
        Button(
            onClick = {
                onUpdate(
                    selectedLocation?.id ?: entry.locationId,
                    weather.takeIf { it.isNotEmpty() },
                    notes.takeIf { it.isNotEmpty() },
                    photoUri,
                    dateEnd?.let { Date(it) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("💾 Сохранить изменения", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showEndDatePicker) {
        val initialDateMillis = dateEnd ?: entry.date.time
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->

                            val oldTime = dateEnd
                                ?.let { Date(it) }
                                ?: entry.date

                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = selectedDate

                                val oldCalendar = Calendar.getInstance().apply {
                                    time = oldTime
                                }

                                set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            dateEnd = calendar.timeInMillis
                        }

                        showEndDatePicker = false
                    }
                ) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
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

    if (showEndTimePicker) {
        val currentEndDate = dateEnd?.let { Date(it) } ?: entry.date
        val calendar = Calendar.getInstance().apply {
            time = currentEndDate
        }

        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = {
                showEndTimePicker = false
            },
            title = { Text("Время окончания рыбалки") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = Calendar.getInstance().apply {
                            time = currentEndDate

                            set(
                                Calendar.HOUR_OF_DAY,
                                timePickerState.hour
                            )

                            set(
                                Calendar.MINUTE,
                                timePickerState.minute
                            )

                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        dateEnd = result.timeInMillis

                        showEndTimePicker = false
                    }
                ) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndTimePicker = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }


}