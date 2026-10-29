package com.fishingpoints.presentation.screens.fish.fishDetail

import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle

/**
 * Представляет все возможные состояния экрана подробностей о рыбе.
 */
data class FishDetailUiState(
    val isLoading: Boolean = false,
    val fish: Fish? = null,
    val baits: List<Bait> = emptyList(),
    val tackles: List<Tackle> = emptyList(),
    val feeds: List<Feed> = emptyList(),
    val errorMessage: String? = null,
    val loadedFishId: Long? = null
)