package com.fishingpoints.presentation.screens.diary

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.core.location.GeocoderService
import com.fishingpoints.core.location.LocationProvider
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.FishingEntryWithLocation
import com.fishingpoints.data.local.entity.GroupedEntries
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.repository.FishingRepository
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.presentation.screens.map.FishingMapRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class FishingDiaryViewModel @Inject constructor(
    private val repository: FishingRepository,
    private val locationRepository: LocationRepository,
    val locationProvider: LocationProvider,
    val geocoderService: GeocoderService
) : ViewModel() {

    private var entriesJob: Job? = null
    private var locationsJob: Job? = null

    val fishingMapRows: StateFlow<List<FishingMapRow>> =
        repository
            .getFishingMapRows()
            .catch { throwable ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = throwable.message
                            ?: "Ошибка загрузки данных карты"
                    )
                }
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    // ============================================================
    // UI STATE
    // ============================================================

    private val _uiState =
        MutableStateFlow(
            FishingDiaryUiState(
                isLoading = true
            )
        )

    val uiState: StateFlow<FishingDiaryUiState> =
        _uiState.asStateFlow()


    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {
        observeEntries()
        observeLocations()
    }


    // ============================================================
    // НАБЛЮДЕНИЕ ЗА ЗАПИСЯМИ
    // ============================================================

    private fun observeEntries() {
        entriesJob?.cancel()

        entriesJob =
            viewModelScope.launch {
                repository
                    .getAllEntriesWithLocation()
                    .catch { throwable ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                errorMessage =
                                    throwable.message
                                        ?: "Ошибка загрузки записей"
                            )
                        }
                    }
                    .collect { entries ->
                        _uiState.update { state ->
                            val filtered =
                                filterEntries(
                                    entries = entries,
                                    query = state.searchQuery
                                )

                            state.copy(
                                entries = entries,
                                filteredEntries =
                                    sortEntries(
                                        entries = filtered,
                                        sortOrder = state.sortOrder
                                    ),
                                groupedEntries =
                                    createGroupedEntries(
                                        entries = filtered,
                                        sortOrder = state.sortOrder
                                    ),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
            }
    }


    // ============================================================
    // НАБЛЮДЕНИЕ ЗА ЛОКАЦИЯМИ
    // ============================================================

    private fun observeLocations() {
        locationsJob?.cancel()
        locationsJob =
            viewModelScope.launch {
                locationRepository
                    .getAllLocations()
                    .catch { throwable ->
                        _uiState.update { state ->
                            state.copy(
                                errorMessage =
                                    throwable.message
                                        ?: "Ошибка загрузки мест"
                            )
                        }
                    }
                    .collect { locations ->
                        _uiState.update { state ->
                            state.copy(
                                locations = locations
                            )
                        }
                    }
            }
    }


    // ============================================================
    // ПОИСК
    // ============================================================

    fun updateSearchQuery(
        query: String
    ) {

        _uiState.update { state ->
            val filtered =
                filterEntries(
                    entries = state.entries,
                    query = query
                )

            state.copy(
                searchQuery = query,
                filteredEntries =
                    sortEntries(
                        entries = filtered,
                        sortOrder = state.sortOrder
                    ),
                groupedEntries =
                    createGroupedEntries(
                        entries = filtered,
                        sortOrder = state.sortOrder
                    )
            )
        }
    }

    fun clearSearch() { updateSearchQuery("") }

    fun openSearch() {
        _uiState.update { state ->
            state.copy(
                showSearch = true
            )
        }
    }

    fun closeSearch() {
        _uiState.update { state ->
            state.copy(
                showSearch = false,
                searchQuery = "",
                filteredEntries =
                    sortEntries(
                        entries = state.entries,
                        sortOrder = state.sortOrder
                    ),
                groupedEntries =
                    createGroupedEntries(
                        entries = state.entries,
                        sortOrder = state.sortOrder
                    )
            )
        }
    }


    // ============================================================
    // СОРТИРОВКА
    // ============================================================
    fun setSortOrder(
        order: SortOrder
    ) {
        _uiState.update { state ->
            val filtered =
                filterEntries(
                    entries = state.entries,
                    query = state.searchQuery
                )

            state.copy(
                sortOrder = order,
                filteredEntries =
                    sortEntries(
                        entries = filtered,
                        sortOrder = order
                    ),

                groupedEntries =
                    createGroupedEntries(
                        entries = filtered,
                        sortOrder = order
                    )
            )
        }
    }


    fun toggleSortOrder() {

        val currentOrder =
            _uiState.value.sortOrder

        val newOrder =
            when (currentOrder) {

                SortOrder.NEWEST_FIRST ->
                    SortOrder.OLDEST_FIRST

                SortOrder.OLDEST_FIRST ->
                    SortOrder.NEWEST_FIRST

                SortOrder.BY_REGION ->
                    SortOrder.NEWEST_FIRST
            }

        setSortOrder(newOrder)
    }


    // ============================================================
    // ФИЛЬТРАЦИЯ
    // ============================================================

    private fun filterEntries(
        entries: List<FishingEntryWithLocation>,
        query: String
    ): List<FishingEntryWithLocation> {

        val normalizedQuery =
            query
                .trim()
                .lowercase()

        if (normalizedQuery.isBlank()) {
            return entries
        }

        return entries.filter { item ->
            val notesMatches =
                item.entry.notes
                    ?.lowercase()
                    ?.contains(normalizedQuery) == true

            val locationMatches =
                item.locationName
                    .lowercase()
                    .contains(normalizedQuery)

            val regionMatches =
                item.regionName
                    .lowercase()
                    .contains(normalizedQuery)

            val weatherMatches =
                item.entry.weather
                    ?.lowercase()
                    ?.contains(normalizedQuery) == true

            notesMatches ||
                    locationMatches ||
                    regionMatches ||
                    weatherMatches
        }
    }


    // ============================================================
    // СОРТИРОВКА ЗАПИСЕЙ
    // ============================================================

    private fun sortEntries(
        entries: List<FishingEntryWithLocation>,
        sortOrder: SortOrder
    ): List<FishingEntryWithLocation> {

        return when (sortOrder) {

            SortOrder.NEWEST_FIRST -> {

                entries.sortedByDescending {
                    it.entry.date
                }
            }

            SortOrder.OLDEST_FIRST -> {

                entries.sortedBy {
                    it.entry.date
                }
            }

            SortOrder.BY_REGION -> {

                entries.sortedWith(

                    compareBy<FishingEntryWithLocation> {
                        it.regionName
                            .trim()
                            .lowercase()
                    }.thenByDescending {

                        it.entry.date
                    }
                )
            }
        }
    }


    // ============================================================
    // ГРУППИРОВКА ПО РЕГИОНАМ
    // ============================================================

    private fun createGroupedEntries(
        entries: List<FishingEntryWithLocation>,
        sortOrder: SortOrder
    ): List<GroupedEntries> {
        if (
            sortOrder != SortOrder.BY_REGION
        ) {
            return emptyList()
        }
        return entries
            .groupBy {
                it.regionName.trim()
            }
            .map { (regionName, regionEntries) ->
                GroupedEntries(
                    regionName = regionName,
                    entries =
                        regionEntries.sortedByDescending {
                            it.entry.date
                        }
                )
            }
            .sortedBy {
                it.regionName.lowercase()
            }
    }


    // ============================================================
    // ДОБАВЛЕНИЕ ЗАПИСИ
    // ============================================================

    fun saveEntry(
        locationId: Long,
        notes: String,
        photoUri: String?,
        weather: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isSaving = true,
                        errorMessage = null
                    )
                }

                val entry =
                    FishingEntry(
                        date = Date(),
                        dateEnd = null,
                        locationId = locationId,
                        weather = weather
                                ?.trim()
                                ?.takeIf {
                                    it.isNotEmpty()
                                },
                        notes =
                            notes
                                .trim()
                                .takeIf {
                                    it.isNotEmpty()
                                },
                        photoUri = photoUri
                    )

                repository.insertEntry(
                    entry
                )


                _uiState.update {
                    it.copy(
                        showAddDialog = false,
                        isSaving = false,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage =
                            e.message
                                ?: "Ошибка сохранения записи"
                    )
                }
            }
        }
    }


    // ============================================================
    // УДАЛЕНИЕ ЗАПИСИ
    // ============================================================

    fun deleteEntry(
        entry: FishingEntry
    ) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(
                    entry
                )

                _uiState.update {
                    it.copy(
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            e.message
                                ?: "Ошибка удаления записи"
                    )
                }
            }
        }
    }


    // ============================================================
    // ДИАЛОГ ДОБАВЛЕНИЯ ЗАПИСИ
    // ============================================================

    fun showAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                errorMessage = null
            )
        }
    }


    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false
            )
        }
    }


    // ============================================================
    // ДИАЛОГ ДОБАВЛЕНИЯ МЕСТА
    // ============================================================
    fun showAddLocationDialog() {
        _uiState.update {
            it.copy(
                showAddLocationDialog = true,
                errorMessage = null
            )
        }
    }


    fun hideAddLocationDialog() {
        _uiState.update {
            it.copy(
                showAddLocationDialog = false
            )
        }
    }


    // ============================================================
    // СОХРАНЕНИЕ МЕСТА
    // ============================================================

    fun saveLocation(
        name: String,
        lat: Double,
        lon: Double
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isSavingLocation = true,
                        errorMessage = null
                    )
                }

                val normalizedName =
                    name
                        .trim()
                        .replace(
                            Regex("\\s+"),
                            " "
                        )

                require(
                    normalizedName.isNotBlank()
                ) {
                    "Название места не может быть пустым"
                }


                // ------------------------------------------------
                // Получаем регион
                // ------------------------------------------------

                val regionName =
                    geocoderService
                        .getRegion(
                            lat = lat,
                            lon = lon
                        )
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Неизвестный регион"


                // ------------------------------------------------
                // Создаём Location через Repository
                // ------------------------------------------------

                val locationId =
                    locationRepository.createLocation(
                        name = normalizedName,
                        lat = lat,
                        lon = lon,
                        zoneId = ZoneId.systemDefault(),
                        regionName = regionName
                    )


                // ------------------------------------------------
                // Возвращаемся к диалогу записи
                // ------------------------------------------------

                _uiState.update {
                    it.copy(
                        newLocationId = locationId,
                        showAddLocationDialog = false,
                        showAddDialog = true,
                        isSavingLocation = false,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingLocation = false,
                        errorMessage =
                            e.message
                                ?: "Ошибка сохранения места"
                    )
                }
            }
        }
    }


    // ============================================================
    // НОВОЕ МЕСТО ПОТРЕБЛЕНО ДИАЛОГОМ
    // ============================================================

    fun clearNewLocationId() {
        _uiState.update {
            it.copy(
                newLocationId = null
            )
        }
    }


    // ============================================================
    // ОШИБКА
    // ============================================================

    fun clearError() {
        _uiState.update {
            it.copy(
                errorMessage = null
            )
        }
    }


    // ============================================================
    // RETRY
    // ============================================================

    fun retry() {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        observeEntries()
        observeLocations()
    }
}


// Перечисление для сортировки по дате
enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    BY_REGION;

    fun getDisplayName(): String {
        return when (this) {
            NEWEST_FIRST -> "Сначала новые"
            OLDEST_FIRST -> "Сначала старые"
            BY_REGION -> "По регионам"
        }
    }

    fun getIcon(): ImageVector {
        return when (this) {
            NEWEST_FIRST -> Icons.AutoMirrored.Filled.TrendingUp
            OLDEST_FIRST -> Icons.AutoMirrored.Filled.TrendingDown
            BY_REGION -> Icons.Default.LocationOn
        }
    }
}


data class FishingDiaryUiState(
    // ЗАПИСИ
    val entries: List<FishingEntryWithLocation> = emptyList(),
    val filteredEntries: List<FishingEntryWithLocation> = emptyList(),
    val groupedEntries: List<GroupedEntries> = emptyList(),

    // ПОИСК
    val searchQuery: String = "",
    val showSearch: Boolean = false,

    // СОРТИРОВКА
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,

    // ЛОКАЦИИ
    val locations: List<LocationEntity> = emptyList(),

    // ДИАЛОГ ДОБАВЛЕНИЯ ЗАПИСИ
    val showAddDialog: Boolean = false,

    // ДИАЛОГ ДОБАВЛЕНИЯ МЕСТА
    val showAddLocationDialog: Boolean = false,

    // НОВОЕ МЕСТО
    val newLocationId: Long? = null,

    // ЗАГРУЗКА
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavingLocation: Boolean = false,

    // ОШИБКА
    val errorMessage: String? = null
)

