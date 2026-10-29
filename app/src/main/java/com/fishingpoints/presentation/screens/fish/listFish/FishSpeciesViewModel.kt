package com.fishingpoints.presentation.screens.fish.listFish


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class FishSpeciesViewModel @Inject constructor(
    private val repository: FishRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSeason = MutableStateFlow<Season?>(null)
    val selectedSeason: StateFlow<Season?> = _selectedSeason.asStateFlow()

    private val _selectedTimeOfDay = MutableStateFlow<TimeOfDay?>(null)
    val selectedTimeOfDay: StateFlow<TimeOfDay?> =
        _selectedTimeOfDay.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishList: StateFlow<List<Fish>> = combine(
        searchQuery
            .debounce(300.milliseconds)
            .distinctUntilChanged(),
        selectedSeason,
        selectedTimeOfDay
    ) { query, season, timeOfDay ->
        Triple(query.trim(), season, timeOfDay)
    }.flatMapLatest { (query, season, timeOfDay) ->
        repository.getFilteredFish(
            query = query,
            season = season,
            timeOfDay = timeOfDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedSeason(season: Season?) {
        _selectedSeason.value = season
    }

    fun updateSelectedTimeOfDay(timeOfDay: TimeOfDay?) {
        _selectedTimeOfDay.value = timeOfDay
    }

    suspend fun deleteFish(fish: Fish): FishRepository.DeletedFishData {
        return repository.deleteFishWithRelations(fish)
    }

    suspend fun undoDeleteFish(deletedFish: FishRepository.DeletedFishData) {
        repository.restoreFish(deletedFish)
    }

}
