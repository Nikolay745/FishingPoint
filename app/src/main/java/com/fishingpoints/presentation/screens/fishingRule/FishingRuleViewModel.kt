package com.fishingpoints.presentation.screens.fishingRule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.core.location.GeocoderService
import com.fishingpoints.core.location.LocationProvider
import com.fishingpoints.core.location.LocationResult
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.local.entity.RegionRule
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.FishingRepository
import com.fishingpoints.data.repository.RegionRepository
import com.fishingpoints.data.repository.RegulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FishingRuleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fishRepository: FishRepository,
    private val repository: FishingRepository,
    private val regionRepository: RegionRepository,
    private val regulationRepository: RegulationRepository,
    private val locationProvider: LocationProvider,
    private val geocoderService: GeocoderService
) : ViewModel() {

    companion object {
        private const val SELECTED_REGION_ID = "selected_region_id"
        private const val SELECTED_DATE = "selected_date"
        private const val SELECTED_YEAR = "selected_year"
    }

    // --------------------------------------------------
    // SELECTED STATE
    // --------------------------------------------------

    private val selectedRegionId =
        savedStateHandle.getStateFlow<Long?>(
            key = SELECTED_REGION_ID,
            initialValue = null
        )


    //    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedDate =
        savedStateHandle.getStateFlow(
            key = SELECTED_DATE,
            initialValue = LocalDate.now()
        )

    private val selectedYear =   savedStateHandle.getStateFlow(
            key = SELECTED_YEAR,
            initialValue = LocalDate.now().year
        )

    private val isDetectingRegion = MutableStateFlow(false)

    private val errorMessage = MutableStateFlow<String?>(null)

    private var regionDetectionJob: Job? = null

    /**
     * Меняется только при явном выборе региона пользователем.
     *
     * Защищает ручной выбор от автоматического определения.
     */
    private var regionSelectedManually = false


    // --------------------------------------------------
    // DATABASE FLOWS
    // --------------------------------------------------

    private val fishFlow =
        fishRepository.getAllFish()

    private val regionFlow =
        regionRepository.getAllRegions()

// --------------------------------------------------
    // DATE-DEPENDENT FLOWS
    // --------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allowanceFlow: Flow<List<DailyAllowance>> =
        combine(
            selectedRegionId,
            selectedDate
        ) { regionId, date ->
            regionId to date
        }
            .distinctUntilChanged()
            .flatMapLatest { (regionId, date) ->

                if (regionId == null) {
                    flowOf(emptyList())
                } else {
                    repository.getDailyAllowanceForDate(
                        regionId = regionId,
                        date = date
                    )
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val regionRuleFlow: Flow<RegionRule?> =
        combine(
            selectedRegionId,
            selectedDate
        ) { regionId, date ->
            regionId to date
        }
            .distinctUntilChanged()
            .flatMapLatest { (regionId, date) ->

                if (regionId == null) {
                    flowOf(null)
                } else {
                    regulationRepository.getRegionRuleForDate(
                        regionId = regionId,
                        date = date
                    )
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val fishingBanFlow: Flow<List<FishingBan>> =
        combine(
            selectedRegionId,
            selectedDate
        ) { regionId, date ->
            regionId to date
        }
            .distinctUntilChanged()
            .flatMapLatest { (regionId, date) ->

                if (regionId == null) {
                    flowOf(emptyList())
                } else {
                    regulationRepository.getFishingBansForDate(
                        regionId = regionId,
                        date = date
                    )
                }
            }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

    private fun sortAllowancesByFishName(
        allowances: List<DailyAllowance>,
        fishList: List<Fish>
    ): List<DailyAllowance> {

        val fishNames = fishList.associate { fish ->
            fish.id to fish.name
        }

        return allowances.sortedWith(
            compareBy<DailyAllowance>(
                { fishNames[it.fishId] ?: "" },
                { it.id }
            )
        )
    }

    // --------------------------------------------------
    // BASE STATE
    // --------------------------------------------------

    private val baseState: Flow<FishLimitUiState> =
        combine(
            fishFlow,
            regionFlow,
            selectedRegionId,
            selectedDate,
            allowanceFlow
        ) { fish, regions, regionId, date, allowances ->

            FishLimitUiState(
                fishList = fish,
                regionList = regions,
                selectedRegionId = regionId,
                selectedDate = date,
                dailyAllowance = sortAllowancesByFishName(
                    allowances = allowances,
                    fishList = fish
                )
            )
        }

    /**
     * Добавляем выбранный год отдельно,
     * не связывая его автоматически с выбранной датой.
     */
    private val baseStateWithYear: Flow<FishLimitUiState> =
        combine(
            baseState,
            selectedYear
        ) { state, year ->

            state.copy(
                selectedYear = year
            )
        }

// --------------------------------------------------
    // UI STATE
    // --------------------------------------------------

    val uiState: StateFlow<FishLimitUiState> =
        combine(
            baseStateWithYear,
            regionRuleFlow,
            fishingBanFlow,
            isDetectingRegion,
            errorMessage
        ) { state, rule, bans, detectingRegion, error ->

            state.copy(
                regionRule = rule,
                fishingBans = bans,
                isDetectingRegion = detectingRegion,
                errorMessage = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FishLimitUiState()
        )

// --------------------------------------------------
    // DATE
    // --------------------------------------------------

    fun selectDate(date: LocalDate) {
        savedStateHandle[SELECTED_DATE] = date
        errorMessage.value = null
    }

    fun showCurrentDate() {
        selectDate(LocalDate.now())
    }
// --------------------------------------------------
    // YEAR
    // --------------------------------------------------

    fun selectYear(year: Int) {
        savedStateHandle[SELECTED_YEAR] = year
    }

    // --------------------------------------------------
    // REGION SELECTION
    // --------------------------------------------------

    fun selectRegion(regionId: Long) {

        // Сначала запрещаем автоматическое изменение региона.
        regionSelectedManually = true

        // Отменяем определение региона, если оно выполняется.
        regionDetectionJob?.cancel()
        regionDetectionJob = null

        isDetectingRegion.value = false
        errorMessage.value = null

        // Сохраняем выбор пользователя.
        savedStateHandle[SELECTED_REGION_ID] = regionId
    }

// --------------------------------------------------
    // SCREEN OPEN
    // --------------------------------------------------

    fun onScreenOpened() {

        // Уже выбранный регион не требуется определять повторно.
        if (selectedRegionId.value != null) {
            return
        }

        // Не запускаем несколько одинаковых задач.
        if (regionDetectionJob?.isActive == true) {
            return
        }

        detectCurrentRegion()
    }

    // --------------------------------------------------
    // AUTOMATIC REGION DETECTION
    // --------------------------------------------------

    fun detectCurrentRegion() {

        if (regionSelectedManually) {
            return
        }

        if (selectedRegionId.value != null) {
            return
        }

        if (regionDetectionJob?.isActive == true) {
            return
        }

        regionDetectionJob = viewModelScope.launch {

            val currentJob = currentCoroutineContext()[Job]

            isDetectingRegion.value = true
            errorMessage.value = null

            try {

                // 1. Получаем текущее местоположение.

                val locationResult =
                    locationProvider.getCurrentLocation()

                val location = when (locationResult) {
                    is LocationResult.Success -> locationResult
                    else -> return@launch
                }

                // 2. Проверяем, не выбрал ли пользователь регион.

                if (regionSelectedManually ||
                    selectedRegionId.value != null
                ) {
                    return@launch
                }

                // 3. Определяем название региона по координатам.

                val regionName = geocoderService.getRegion(
                    lat = location.latitude,
                    lon = location.longitude
                ) ?: return@launch

                // 4. Повторно проверяем актуальность выбора.

                if (regionSelectedManually ||
                    selectedRegionId.value != null
                ) {
                    return@launch
                }

                // 5. Ищем регион в базе данных.

                val region = regionRepository
                    .getRegionsByNameContains(regionName)
                    .firstOrNull()

                // 6. Записываем результат только при отсутствии
                //    ручного выбора и ранее установленного региона.

                if (
                    region != null &&
                    !regionSelectedManually &&
                    selectedRegionId.value == null
                ) {
                    savedStateHandle[SELECTED_REGION_ID] = region.id
                }

            } catch (e: CancellationException) {
                // Отмена корутины должна распространяться дальше.
                throw e

            } catch (e: Exception) {
                errorMessage.value =
                    "Не удалось автоматически определить регион."

            } finally {

                /*
                 * Старая отменённая задача не должна сбрасывать
                 * состояние новой задачи.
                 */
                if (regionDetectionJob === currentJob) {
                    regionDetectionJob = null
                    isDetectingRegion.value = false
                }
            }
        }
    }

// --------------------------------------------------
    // DAILY ALLOWANCE CRUD
    // --------------------------------------------------

    fun insertDailyAllowance(
        allowance: DailyAllowance
    ) {
        viewModelScope.launch {
            repository.insertDailyAllowance(allowance)
        }
    }

    fun updateDailyAllowance(
        allowance: DailyAllowance
    ) {
        viewModelScope.launch {
            repository.updateDailyAllowance(allowance)
        }
    }

    fun deleteDailyAllowance(
        allowance: DailyAllowance
    ) {
        viewModelScope.launch {
            repository.deleteDailyAllowance(allowance)
        }
    }

    // --------------------------------------------------
    // FISHING BAN CRUD
    // --------------------------------------------------

    fun insertFishingBan(
        ban: FishingBan
    ) {
        viewModelScope.launch {
            regulationRepository.insertFishingBan(ban)
        }
    }

    fun updateFishingBan(
        ban: FishingBan
    ) {
        viewModelScope.launch {
            regulationRepository.updateFishingBan(ban)
        }
    }

    fun deleteFishingBan(
        ban: FishingBan
    ) {
        viewModelScope.launch {
            regulationRepository.deleteFishingBan(ban)
        }
    }

    // --------------------------------------------------
    // REGION RULE CRUD
    // --------------------------------------------------

    fun insertRegionRule(
        rule: RegionRule
    ) {
        viewModelScope.launch {
            regulationRepository.insertRegionRule(rule)
        }
    }

    fun updateRegionRule(
        rule: RegionRule
    ) {
        viewModelScope.launch {
            regulationRepository.updateRegionRule(rule)
        }
    }

    fun deleteRegionRule(
        rule: RegionRule
    ) {
        viewModelScope.launch {
            regulationRepository.deleteRegionRule(rule)
        }
    }

    // --------------------------------------------------
    // LIFECYCLE
    // --------------------------------------------------

    override fun onCleared() {
        regionDetectionJob?.cancel()
        regionDetectionJob = null

        super.onCleared()
    }
}

data class FishLimitUiState(
    val fishList: List<Fish> = emptyList(),
    val regionList: List<Region> = emptyList(),

    val selectedRegionId: Long? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedYear: Int = LocalDate.now().year,

    val regionRule: RegionRule? = null,
    val dailyAllowance: List<DailyAllowance> = emptyList(),
    val fishingBans: List<FishingBan> = emptyList(),

    val isLoading: Boolean = false,
    val isDetectingRegion: Boolean = false,
    val errorMessage: String? = null
)