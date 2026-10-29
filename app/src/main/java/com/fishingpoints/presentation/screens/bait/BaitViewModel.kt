package com.fishingpoints.presentation.screens.bait

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.BaitType
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.repository.BaitRepository
import com.fishingpoints.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaitViewModel @Inject constructor(
    private val repository: BaitRepository,
    private val fishRepository: FishRepository
) : ViewModel() {
    val baits = repository.baits
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

    private val _errorEvent = Channel<String>()
    val errorEvent = _errorEvent.receiveAsFlow()

    fun addBait(
        name: String,
        type: BaitType,
        season: Season,
        selectedFish: List<Fish>,
        description: String
    ) {
        viewModelScope.launch {

            val cleanName = name.trim()

            if (cleanName.isBlank()) {
                _errorEvent.send("Введите название насадки")
                return@launch
            }

            val existing = repository.getBaitsByName(cleanName)

            if (existing != null) {
                _errorEvent.send(
                    "Насадка «$cleanName» уже существует"
                )
                return@launch
            }

            val baitId = repository.insert(
                Bait(
                    name = cleanName,
                    type = type,
                    bestSeason = season,
                    description = description
                )
            )

            val crossRefs = selectedFish.map { fish ->
                BaitFishCrossRef(
                    baitId = baitId,
                    fishId = fish.id
                )
            }

            if (crossRefs.isNotEmpty()) {
                repository.insertBaitFishCrossRefs(crossRefs)
            }
        }
    }

    fun updateBait(
        bait: Bait,
        selectedFish: List<Fish>
    ) {
        viewModelScope.launch {

            val cleanName = bait.name.trim()

            val existing = repository.getBaitsByName(cleanName)

            if (existing != null && existing.id != bait.id) {
                _errorEvent.send(
                    "Насадка «$cleanName» уже существует"
                )
                return@launch
            }

            val crossRefs = selectedFish.map { fish ->
                BaitFishCrossRef(
                    baitId = bait.id,
                    fishId = fish.id
                )
            }

            repository.updateBaitWithFish(
                bait = bait.copy(name = cleanName),
                crossRefs = crossRefs
            )
        }
    }

    fun deleteBait(bait: Bait) {
        viewModelScope.launch {
            repository.delete(bait)
        }
    }

    suspend fun getFishIdsForBait(baitId: Long): List<Long> {
        return repository.getFishIdsForBait(baitId)
    }
}