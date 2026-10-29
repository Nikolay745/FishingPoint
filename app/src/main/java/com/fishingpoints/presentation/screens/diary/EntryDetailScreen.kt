package com.fishingpoints.presentation.screens.diary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fishingpoints.core.utils.FileUtil
import com.fishingpoints.presentation.components.FullScreenPhotoViewer
import com.fishingpoints.presentation.screens.diary.cards.CatchCard
import com.fishingpoints.presentation.screens.diary.cards.SummaryCard
import com.fishingpoints.presentation.screens.diary.dialogs.AddCatchDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    navController: NavController,
    entryId: Long,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var catchToEdit by remember { mutableStateOf<CatchWithDetails?>(null) }

    val entry by viewModel.entry.collectAsState()
    val context = LocalContext.current

    val catches by viewModel.catches.collectAsState()
    val fishList by viewModel.fishList.collectAsState()
    val baitList by viewModel.baitList.collectAsState()
    val feedList by viewModel.feedList.collectAsState()
    val tackleList by viewModel.tackleList.collectAsState()
    val showAddCatchDialog by viewModel.showAddCatchDialog.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val catchPhotos by viewModel.catchPhotos.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        uri ?: return@rememberLauncherForActivityResult

        val newPhotoPath = FileUtil.copyToInternalStorage(
            context = context,
            sourceUri = uri
        )

        if (newPhotoPath != null) {
            viewModel.saveCatchPhoto(newPhotoPath)
        }
    }

    LaunchedEffect(entryId) { viewModel.loadEntry(entryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Детали рыбалки") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Добавить фото улова"
                        )
                    }


                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.showAddCatchDialog()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить улов"
                )
            }
        }
    ) { paddingValues ->

        entry?.let { fishingEntry ->
            val dateFormatter = remember {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            }

            val timeFormatter = remember {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Информация о рыбалке
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 ${dateFormatter.format(fishingEntry.date)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "📍 ${locationName ?: "Загрузка места..."}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🕐 Начало: ${timeFormatter.format(fishingEntry.date)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "🏁 Окончание: ${
                                            fishingEntry.dateEnd?.let {
                                                timeFormatter.format(it)
                                            } ?: "не указано"
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (fishingEntry.dateEnd != null) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            if (!fishingEntry.notes.isNullOrEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    text = "📝 Заметки:",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = fishingEntry.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Уловы
                if (catches.isNotEmpty()) {
                    item {
                        Text(
                            text = "🐟 Уловы",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Если есть фото
                    if (catchPhotos.isNotEmpty()) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📸 Фото улова",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(File(catchPhotos.first()))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Фото улова",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            showFullScreenPhoto = true
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    items(catches) { catchItem ->
                        CatchCard(
                            catchWithDetails = catchItem,
                            onEdit = {
                                catchToEdit = catchItem
                            },
                            onDelete = {
                                viewModel.deleteCatch(catchItem.catch)
                            }
                        )
                    }


                    item {
                        SummaryCard(catches = catches)
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Нет записей об уловах",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Нажмите на + чтобы добавить рыбу",
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

    if (showFullScreenPhoto && catchPhotos.isNotEmpty()) {

        FullScreenPhotoViewer(
            photoUri = catchPhotos.first(),
            onDismiss = { showFullScreenPhoto = false }
        )
    }

    if (showAddCatchDialog) {
        AddCatchDialog(
            fishList = fishList,
            baitList = baitList.map { it.bait },
            feedList = feedList,
            tackleList = tackleList,
            onDismiss = { viewModel.hideAddCatchDialog() },
            onSave = { fishId, baitId, feedId, tackleId, count, weight, length,  caughtAt ->
                viewModel.saveCatch(fishId, baitId, feedId, tackleId, count, weight, length, caughtAt)
            }
        )
    }

    catchToEdit?.let { catchItem ->
        AddCatchDialog(
            fishList = fishList,
            baitList = baitList.map { it.bait },
            feedList = feedList,
            tackleList = tackleList,
            editingCatch = catchItem,
            onDismiss = { catchToEdit = null },
            onSave = { fishId, baitId, feedId, tackleId, count, weight, length,
                       caughtAt ->
                viewModel.updateCatch(
                    catch = catchItem.catch,
                    fishId = fishId,
                    baitId = baitId,
                    feedId = feedId,
                    tackleId = tackleId,
                    count = count,
                    weight = weight,
                    length = length,
                    caughtAt = caughtAt
                )

                catchToEdit = null
            }
        )
    }


}