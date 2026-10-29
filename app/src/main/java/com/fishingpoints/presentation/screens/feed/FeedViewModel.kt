package com.fishingpoints.presentation.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.repository.FeedRepository
import com.fishingpoints.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val fishRepository: FishRepository
) : ViewModel() {
    val feeds = repository.feeds
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allFish = fishRepository.getAllFish()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }


    fun addFeed(
        name: String,
        season: Season,
        fishNames: String,
        description: String
    ) {
        viewModelScope.launch {
            val existing = repository.getFeedByName(name.trim())
            if (existing != null) {
                // Здесь нужно передать событие ошибки в UI (см. ниже)
                _uiState.update { it.copy(error = "Прикормка с таким названием уже существует") }
                return@launch
            }

            val feedId =repository.insert(
                Feed(
                    name = name,
                    bestSeason = season,
                    bestForFish = fishNames,
                    description = description
                )
            )

            if (feedId == -1L) {
                _uiState.update { it.copy(error = "Не удалось сохранить прикормку") }
                return@launch
            }

            // Сохраняем связи с рыбами
            val fishList = fishRepository.getFishByNameList(fishNames.split(",").map { it.trim() })
            fishList.forEach { fish ->
                repository.insertFeedFishCrossRef(
                    FeedFishCrossRef(
                        feedId = feedId,
                        fishId = fish.id
                    )
                )
            }
            _uiState.update { it.copy(error = null) }
        }
    }

fun updateFeed(feed: Feed, fishNames: String) {
    viewModelScope.launch {
        val newName = feed.name.trim()
        val existing = repository.getFeedByName(newName)

        // Разрешаем, если это та же самая запись (по ID)
        if (existing != null && existing.id != feed.id) {
            _uiState.update { it.copy(error = "Прикормка с таким названием уже существует") }
            return@launch
        }


        repository.update(feed.copy(name = newName))

        // Обновляем связи с рыбами
        // Сначала удаляем старые связи
        repository.deleteFeedFishCrossRefs(feed.id)

        // Затем добавляем новые
        val fishList = fishRepository.getFishByNameList(fishNames.split(",").map { it.trim() })
        fishList.forEach { fish ->
            repository.insertFeedFishCrossRef(
                FeedFishCrossRef(
                    feedId = feed.id,
                    fishId = fish.id
                )
            )
        }
    }
}

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch {
            repository.delete(feed)
        }
    }

    suspend fun getFishIdsForFeed(feedId: Long): List<Long> {
        return repository.getFishIdsForFeed(feedId)
    }

}

data class FeedUiState(
    val feeds: List<Feed> = emptyList(),
    val allFish: List<Fish> = emptyList(),
    val error: String? = null,
    // другие поля...
)
