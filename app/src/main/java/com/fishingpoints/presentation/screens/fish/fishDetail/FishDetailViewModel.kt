package com.fishingpoints.presentation.screens.fish.fishDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.repository.BaitRepository
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.TackleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FishDetailViewModel @Inject constructor(
    private val fishRepository: FishRepository,
    private val baitRepository: BaitRepository,
    private val tackleRepository: TackleRepository,
    private val feedDao: FeedDao
) : ViewModel() {

    // --- 1. Единый StateFlow ---
    // Приватный изменяемый поток для внутреннего использования
    private val _uiState = MutableStateFlow(FishDetailUiState())
    // Публичный неизменяемый поток для UI
    val uiState: StateFlow<FishDetailUiState> = _uiState.asStateFlow()

    fun loadFishDetails(fishId: Long) {
        val current = _uiState.value
        if (current.isLoading || current.loadedFishId == fishId) return

        viewModelScope.launch {
            // 1. Устанавливаем состояние "загрузка"
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val fishData = fishRepository.getFishById(fishId)
                val seasons = fishRepository.getSeasonsForFish(fishId)
                val bestTimeOfDay = fishRepository.getTimeOfDayForFish(fishId)

                val fish = fishData?.copy(
                    seasons = seasons,
                    bestTimeOfDay = bestTimeOfDay
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fish = fish,
                        baits = baitRepository.getBaitsForFish(fishId),
                        tackles = tackleRepository.getTacklesForFish(fishId),
                        feeds = feedDao.getFeedForFish(fishId),
                        loadedFishId = fishId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
}