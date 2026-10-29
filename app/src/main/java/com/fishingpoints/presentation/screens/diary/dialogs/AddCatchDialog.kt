package com.fishingpoints.presentation.screens.diary.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.presentation.screens.diary.CatchWithDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCatchDialog(
    fishList: List<Fish>,
    baitList: List<Bait>,
    feedList: List<Feed>,
    tackleList: List<Tackle>,
    editingCatch: CatchWithDetails? = null,
    onDismiss: () -> Unit,
    onSave: (fishId: Long, baitId: Long?, feedId: Long?, tackleId: Long?, count: Int, weight: Long,
             length: Int?,caughtAt: Long) -> Unit
) {

    var selectedFish by remember(editingCatch, fishList) {
        mutableStateOf(
            editingCatch?.let { catch ->
                fishList.find { it.id == catch.catch.fishId }
            }
        )
    }

    var selectedBait by remember(editingCatch, baitList) {
        mutableStateOf(
            editingCatch?.let { catch ->
                catch.catch.baitId?.let { id ->
                    baitList.find { it.id == id }
                }
            }
        )
    }

    var selectedFeed by remember(editingCatch, feedList) {
        mutableStateOf(
            editingCatch?.let { catch ->
                catch.catch.feedId?.let { id ->
                    feedList.find { it.id == id }
                }
            }
        )
    }

    var selectedTackle by remember(editingCatch, tackleList) {
        mutableStateOf(
            editingCatch?.let { catch ->
                catch.catch.tackleId?.let { id ->
                    tackleList.find { it.id == id }
                }
            }
        )
    }

    var count by remember(editingCatch) {
        mutableStateOf(
            editingCatch?.catch?.count?.toString() ?: ""
        )
    }

    var weight by remember(editingCatch) {
        mutableStateOf(
            editingCatch?.catch?.totalWeight?.toString() ?: ""
        )
    }

    var length by remember(editingCatch) {
        mutableStateOf(
            editingCatch?.catch?.avgLength?.toString() ?: ""
        )
    }


    val lengthDouble by remember(length) { derivedStateOf { length.toDoubleOrNull()?.takeIf { it > 0 } } }
    var showFishList by remember { mutableStateOf(false) }
    var showBaitList by remember { mutableStateOf(false) }
    var showFeedList by remember { mutableStateOf(false) }
    var showTackleList by remember { mutableStateOf(false) }

    var caughtAt by remember(editingCatch) {
        mutableLongStateOf(
            editingCatch?.catch?.caughtAt
                ?: System.currentTimeMillis()
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }



    // Состояние нижнего листа
    val sheetState = rememberModalBottomSheetState()

    val onFishSelected: (Fish) -> Unit = { fish ->
        selectedFish = fish
        showFishList = false
    }

    val onBaitSelected: (Bait) -> Unit = { bait ->
        selectedBait = bait
        showBaitList = false
    }

    val onFeedSelected: (Feed) -> Unit = { feed ->
        selectedFeed = feed
        showFeedList = false
    }

    val onTackleSelected: (Tackle) -> Unit = { tackle ->
        selectedTackle = tackle
        showTackleList = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (editingCatch == null) {
                    "🐟 Добавить улов"
                } else {
                    "✏️ Редактировать улов"
                }
            )
        },

        text = {
            Column( modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка выбора рыбы
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFishList = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text( text = "Вид рыбы", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary )
                            Text( text = selectedFish?.name ?: "Не выбрано", style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedFish != null) FontWeight.Bold else FontWeight.Normal
                            )

                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTimePicker = true
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Время поимки",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = timeFormatter.format(Date(caughtAt)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Выбрать время"
                        )
                    }
                }


                HorizontalDivider()

                // Поля ввода
                TextField(
                    value = count,
                    onValueChange = { newText ->
                        // Оставляем только цифры
                        val sanitized = newText.filter { it.isDigit() }
                        if (sanitized != count) {
                            count = sanitized
                        }
                    },
                    label = { Text("Количество (шт.)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = count.isNotBlank() && (count.toIntOrNull() == null || (count.toIntOrNull()
                        ?: 0) <= 0),
                    supportingText = {
                        if (count.isNotBlank() && (count.toIntOrNull() == null || (count.toIntOrNull()
                                ?: 0) <= 0)
                        ) {
                            Text("Введите положительное число")
                        }
                    }
                )

                TextField(
                    value = weight,
                    onValueChange = { newText ->
                        weight = newText.filter { it.isDigit() }
                    },

                            label = { Text("Общий вес (граммы)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = weight.isNotBlank() && (weight.toIntOrNull() == null || (weight.toIntOrNull()
                        ?: 0) <= 0),
                    supportingText = {
                        if (weight.isNotBlank() && (weight.toIntOrNull() == null || (weight.toIntOrNull()
                                ?: 0) <= 0)
                        ) {
                            Text("Введите положительное число")
                        }
                    }
                )

                TextField(
                    value = length,
                    onValueChange = { newText ->
                        length = newText.filter { it.isDigit() }
                    },

                            label = { Text("Средняя длина (см) - опционально") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = length.isNotBlank() && lengthDouble == null,
                    supportingText = {
                        if (length.isNotBlank() && lengthDouble == null) {
                            Text("Введите положительное число")
                        }
                    }
                )
                Text("Использовались:")
                // Кнопка выбора Насадка
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBaitList = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text( text = "Насадка", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary )
                            Text( text = selectedBait?.name ?: "Не выбрано", style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedBait != null) FontWeight.Bold else FontWeight.Normal
                            )

                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }


                // Прикормка
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFeedList = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text( text = "Прикормка", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary )
                            Text( text = selectedFeed?.name ?: "Не выбрано", style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedFeed != null) FontWeight.Bold else FontWeight.Normal
                            )

                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                // Орудия лова
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTackleList= true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text( text = "Орудия лова", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary )
                            Text( text = selectedTackle?.name ?: "Не выбрано", style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedTackle!= null) FontWeight.Bold else FontWeight.Normal
                            )

                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val countInt = count.toIntOrNull()
                    val weightInt = weight.toLongOrNull()

                    selectedFish?.let { fish ->
                        countInt?.let { c ->
                            weightInt?.let { w ->
                                onSave(
                                    fish.id,
                                    selectedBait?.id ,
                                    selectedFeed?.id ,
                                    selectedTackle?.id ,
                                    c,
                                    w,
                                    lengthDouble?.toInt(),
                                    caughtAt
                                )
                            }
                        }
                    }
                },
                enabled = selectedFish != null &&
                        count.toIntOrNull()?.let { it > 0 } == true &&
                        weight.toIntOrNull()?.let { it > 0 } == true &&
                        // Длина опциональна, но если введена — должна быть числом
                        (length.isBlank() || lengthDouble != null)
            ) {
                Text(
                    if (editingCatch == null) {
                        "Добавить"
                    } else {
                        "Сохранить изменения"
                    }
                )

            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showTimePicker) {
        val initialCalendar = remember(caughtAt) {
            Calendar.getInstance().apply {
                timeInMillis = caughtAt
            }
        }

        val timePickerState = rememberTimePickerState(
            initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCalendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            title = {
                Text("Время поимки")
            },
            text = {
                TimePicker(
                    state = timePickerState
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = caughtAt
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

                        caughtAt = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }




    if (showFishList) {
        ModalBottomSheet(
            onDismissRequest = {
                showFishList = false
            },
            sheetState = sheetState
        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredFish by remember(searchQuery, fishList) {
                    derivedStateOf {
                        if (searchQuery.isEmpty()) {
                            fishList
                        } else {
                            fishList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск рыбы") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (filteredFish.isEmpty()) {
                        Text("Рыба не найдена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        // 2. LazyColumn — предпочтительный вариант для больших списков (эффективная виртуализация).
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredFish.size) { index ->
                                val fish = filteredFish[index]
                                FishItemCard(
                                    fish = fish,
                                    isSelected = selectedFish?.id == fish.id,
                                    onClick = { onFishSelected(fish) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBaitList) {
        ModalBottomSheet(
            onDismissRequest = {
                showBaitList = false
            },
            sheetState = sheetState
        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredBait by remember(searchQuery, baitList) {
                    derivedStateOf {
                        if (searchQuery.isEmpty()) {
                            baitList
                        } else {
                            baitList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск насадки и наживки") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (filteredBait.isEmpty()) {
                        Text("насадки и наживки  не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        // 2. LazyColumn — предпочтительный вариант для больших списков (эффективная виртуализация).
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBait.size) { index ->
                                val bait = filteredBait[index]
                                BaitItemCard(
                                    bait = bait,
                                    isSelected = selectedBait?.id == bait.id,
                                    onClick = { onBaitSelected(bait) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFeedList) {
        ModalBottomSheet(
            onDismissRequest = {
                showFeedList = false
            },
            sheetState = sheetState
        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredFeed by remember(searchQuery, feedList) {
                    derivedStateOf {
                        if (searchQuery.isEmpty()) {
                            feedList
                        } else {
                            feedList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск приманки/прикормки") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (filteredFeed.isEmpty()) {
                        Text("Приманки/прикормки  не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredFeed.size) { index ->
                                val feed = filteredFeed[index]
                                FeedItemCard(
                                    feed = feed,
                                    isSelected = selectedFeed?.id == feed.id,
                                    onClick = { onFeedSelected(feed) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTackleList) {
        ModalBottomSheet(
            onDismissRequest = {
                showTackleList = false
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredTackle by remember(searchQuery, tackleList) {
                    derivedStateOf {
                        if (searchQuery.isEmpty()) {
                            tackleList
                        } else {
                            tackleList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск Орудия лова") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (filteredTackle.isEmpty()) {
                        Text("Орудия лова  не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTackle.size) { index ->
                                val tackle = filteredTackle[index]
                                TackleItemCard (
                                    tackle = tackle,
                                    isSelected = selectedTackle?.id == tackle.id,
                                    onClick = { onTackleSelected(tackle) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun FishItemCard(
    fish: Fish,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(fish.name, fontWeight = FontWeight.Bold)
                if (fish.imageUrl != null) {
                    AsyncImage(
                        model = fish.imageUrl,
                        contentDescription = fish.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

        }
    }
}

@Composable
private fun BaitItemCard(
    bait: Bait,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(bait.name, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FeedItemCard(
    feed: Feed,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(feed.name, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TackleItemCard(
    tackle: Tackle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tackle.name, fontWeight = FontWeight.Bold)
            }
        }
    }
}