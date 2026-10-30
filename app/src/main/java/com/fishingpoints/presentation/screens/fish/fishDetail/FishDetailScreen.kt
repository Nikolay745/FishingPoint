package com.fishingpoints.presentation.screens.fish.fishDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitWithFish
import com.fishingpoints.presentation.screens.bait.BaitInfoDialog
import com.fishingpoints.presentation.screens.fish.fishDetail.cards.BaitCard
import com.fishingpoints.presentation.screens.fish.fishDetail.cards.FishInfoCard
import com.fishingpoints.presentation.screens.fish.fishDetail.cards.InfoCard
import com.fishingpoints.presentation.screens.fish.fishDetail.cards.ParametersCard


@Composable
fun FishDetailScreen(
    navController: NavController,
    fishId: Long,
    viewModel: FishDetailViewModel = hiltViewModel()
) {
    // Коллекционируем состояния из ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(fishId) { viewModel.loadFishDetails(fishId) }

    // Обработка состояний через один объект
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.fish != null -> FishDetailContent(
            state = uiState,
            navController = navController
        )
        uiState.errorMessage != null -> {
            ErrorScreen(uiState.errorMessage!!)
        }

        else -> {
            ErrorScreen("Рыба не найдена")
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Ошибка: $message",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FishDetailContent(
    state: FishDetailUiState,
    navController: NavController
) {
    val fish = state.fish ?: return
    var selectedBait by remember { mutableStateOf<Bait?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(fish.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("fish_edit?fishId=${fish.id}") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FishInfoCard(fish) }
            item { ParametersCard(fish) }

            if (state.tackles.isNotEmpty()) {
                item {
                    InfoCard("🎣 Снасти") {
                        state.tackles.forEach { NameRow(it.name) }
                    }
                }
            }

            fish.tipsBaits?.takeIf { it.isNotBlank() }?.let { tips ->
                item {
                    InfoCard("\uD83E\uDEB1 Наживки/насадки") {
                        Text(tips)
                    }
                }
            }

            if (state.feeds.isNotEmpty()) {
                item {
                    InfoCard("🌾 Прикормка") {
                        state.feeds.forEach { NameRow(it.name) }
                    }
                }
            }
//            if (state.baits.isNotEmpty()) {
//                item {
//                    InfoCard("🪱 Рекомендуемые насадки") {
////                        state.baits.forEach { BaitCard(it) }
//                        state.baits.forEach { bait ->
//                            BaitCard(
//                                bait = bait,
//                                onClick = {
//                                    navController.navigate("bait_detail/${bait.id}")
//                                }
//                            )
//                        }
//
//                    }
//                }
//            }
            if (state.baits.isNotEmpty()) {
                item {
                    InfoCard("🪱 Рекомендуемые насадки") {
                        state.baits.forEach { bait ->
                            BaitCard(
                                bait = bait,
                                onClick = {
                                    selectedBait = bait
                                }
                            )
                        }
                    }
                }
            }


            fish.adviceFromFishermen?.takeIf { it.isNotBlank() }?.let { advice ->
                item {
                    InfoCard("Советы опытных рыбаков") {
                        Text(advice)
                    }
                }
            }
        }
    }
    selectedBait?.let { bait ->

        BaitInfoDialog(
            baitWithFish = BaitWithFish(
                bait = bait,
                fish = listOf(fish)
            ),
            onDismiss = {
                selectedBait = null
            }
        )
    }

}

@Composable
private fun NameRow(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}


