package com.fishingpoints.presentation.screens.diary.dialogs

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fishingpoints.core.utils.FileUtil
import com.fishingpoints.data.local.entity.LocationEntity
import java.io.File

@SuppressLint("DefaultLocale")
@Composable
fun AddFishingEntryDialog(
    locations: List<LocationEntity>,
    newLocationId: Long?,
    onNewLocationConsumed: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        locationId: Long,
        notes: String,
        photoUri: String?,
        weather: String?
    ) -> Unit,
    onAddNewLocation: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var weather by rememberSaveable { mutableStateOf("") }
    var showLocationList by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var showWeatherDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current


    // ============================================================
    // Автоматически выбираем только что созданное место
    // ============================================================
    LaunchedEffect(
        newLocationId,
        locations
    ) {

        if (newLocationId == null) {
            return@LaunchedEffect
        }

        val newLocation = locations.firstOrNull { it.id == newLocationId }

        if (newLocation != null) {
            selectedLocation = newLocation
            onNewLocationConsumed()
        }
    }


    // ============================================================
    // Photo Picker
    // ============================================================
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

            // Новое фото успешно скопировано.
            // Теперь старое можно безопасно удалить.
            FileUtil.deleteFile(photoUri)

            // Сохраняем путь нового фото.
            photoUri = newPhotoPath

        } else {

            // Новое фото не скопировалось.
            // Старое фото оставляем.
        }
    }

    // ============================================================
    // Диалог выбора места
    // ============================================================

    if (showLocationList) {
        Dialog(
            onDismissRequest = {
                showLocationList = false
            }
        ) {

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

                    Text(
                        text = "Всего мест: ${locations.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Поиск места") },
                        placeholder = { Text("Введите название места") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )


                    Button(
                        onClick = {
                            showLocationList = false
                            onAddNewLocation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Добавить новое место")
                    }

                    HorizontalDivider()

                    val filteredLocations =
                        if (searchQuery.isBlank()) {
                            locations
                        } else {
                            locations.filter {
                                it.name.contains(
                                    searchQuery,
                                    ignoreCase = true
                                )
                            }
                        }


                    if (filteredLocations.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme
                                            .surfaceVariant
                                )
                        ) {
                            Text(
                                text = "Места не найдены",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                    } else {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        max = 500.dp
                                    ),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            items(
                                items = filteredLocations,
                                key = { it.id }
                            ) { location ->

                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedLocation = location
                                                showLocationList = false
                                                searchQuery = ""
                                            },

                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                if (
                                                    selectedLocation?.id ==
                                                    location.id
                                                ) {
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primaryContainer
                                                } else {
                                                    MaterialTheme
                                                        .colorScheme
                                                        .surface
                                                }
                                        )
                                ) {

                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = location.name,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "📍 ${
                                                String.format("%.4f", location.lat)
                                            }, ${String.format("%.4f", location.lon)}",
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


    // ============================================================
    // Диалог погоды
    // ============================================================
    if (showWeatherDialog) {

        AlertDialog(
            onDismissRequest = {
                showWeatherDialog = false
            },

            title = { Text("🌤 Погода") },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

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

                    val weatherTemplates =
                        listOf(
                            "☀️ Солнечно, 25°C",
                            "⛅ Облачно, 18°C",
                            "🌧 Дождь, 12°C",
                            "🌤 Переменная облачность, 20°C",
                            "💨 Ветрено, 15°C"
                        )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            items = weatherTemplates
                        ) { template ->

                            FilterChip(
                                selected = false,
                                onClick = { weather = template },
                                label = { Text(template) }
                            )
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showWeatherDialog = false
                    }
                ) {
                    Text("Готово")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showWeatherDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }


    // ============================================================
    // Основной диалог
    // ============================================================
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись о рыбалке") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ------------------------------------------------
                // Место
                // ------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showLocationList = true
                        },

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (
                                    selectedLocation != null
                                ) {
                                    MaterialTheme.colorScheme
                                        .primaryContainer
                                } else {
                                    MaterialTheme.colorScheme
                                        .surfaceVariant
                                }
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = "Место рыбалки",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = selectedLocation?.name ?: "Не выбрано",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight =
                                    if (
                                        selectedLocation != null
                                    ) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                            )
                        }

                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription =
                                "Выбрать место"
                        )
                    }
                }

                // ------------------------------------------------
                // Погода
                // ------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showWeatherDialog = true
                        },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (weather.isNotEmpty()) {
                                    MaterialTheme.colorScheme
                                        .secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme
                                        .surfaceVariant
                                }
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                color =
                                    if (
                                        weather.isNotEmpty()
                                    ) {
                                        MaterialTheme.colorScheme
                                            .onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                    }
                            )
                        }

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать погоду",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // ------------------------------------------------
                // Фото
                // ------------------------------------------------
                if (!photoUri.isNullOrEmpty()) {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                    ) {

                        AsyncImage(
                            model =
                                ImageRequest.Builder(
                                    context
                                )
                                    .data(
                                        File(photoUri!!)
                                    )
                                    .crossfade(true)
                                    .build(),

                            contentDescription = "Предпросмотр фото",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = {
                                FileUtil.deleteFile(photoUri)
                                photoUri = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme
                                        .surface
                                        .copy(alpha = 0.8f),

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


                // ------------------------------------------------
                // Добавить фото
                // ------------------------------------------------
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts
                                    .PickVisualMedia
                                    .ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector =
                            if (photoUri == null) {
                                Icons.Default.PhotoCamera
                            } else {
                                Icons.Default.Photo
                            },
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (photoUri == null) {
                            "Добавить фото места ловли"
                        } else {
                            "Изменить фото"
                        }
                    )
                }


                // ------------------------------------------------
                // Заметки
                // ------------------------------------------------
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    selectedLocation?.let { location ->
                        onSave(
                            location.id,
                            notes,
                            photoUri,
                            weather
                                .trim()
                                .takeIf {
                                    it.isNotBlank()
                                }
                        )
                    }
                },

                enabled = selectedLocation != null
            ) {
                Text("Сохранить")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}
