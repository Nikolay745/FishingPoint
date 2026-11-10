package com.fishingpoints.presentation.screens.fish.fishDetail

import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.domain.service.FishActivityResult

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
    val activityError: String? = null,
    val loadedFishId: Long? = null,

    // Температура воды
    val waterTemperature: Double? = null,
    val isEstimatedTemperature: Boolean = false,

    // Условия рыбалки
    val selectedSeason: Season? = null,
    val selectedTimeOfDay: TimeOfDay? = null,
    val selectedRegionId: Long? = null,

    // Результат расчёта
    val activityResult: FishActivityResult? = null,
    val isActivityLoading: Boolean = false
)


