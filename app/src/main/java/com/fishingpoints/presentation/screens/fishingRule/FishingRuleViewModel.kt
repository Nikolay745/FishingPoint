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

        private const val REGION_SELECTED_MANUALLY = "region_selected_manually"


    }

    // --------------------------------------------------
    // SELECTED STATE
    // --------------------------------------------------

    private val selectedRegionId =
        savedStateHandle.getStateFlow<Long?>(
            key = SELECTED_REGION_ID,
            initialValue = null
        )

    private val selectedDate =
        savedStateHandle.getStateFlow(
            key = SELECTED_DATE,
            initialValue = LocalDate.now()
        )

    private val isDetectingRegion = MutableStateFlow(false)

    private val errorMessage = MutableStateFlow<String?>(null)

    private var regionDetectionJob: Job? = null

    /**
     * Меняется только при явном выборе региона пользователем.
     *
     * Защищает ручной выбор от автоматического определения.
     */
    private val regionSelectedManually =
        savedStateHandle.getStateFlow(
            REGION_SELECTED_MANUALLY,
            false
        )



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
    private val regionRulesFlow: Flow<List<RegionRule>> =
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
                    regulationRepository.getRegionRulesForDate(
                        regionId = regionId,
                        date = date
                    )
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allRegionRulesFlow: Flow<List<RegionRule>> =
        selectedRegionId
            .flatMapLatest { regionId ->

                if (regionId == null) {
                    flowOf(emptyList())
                } else {
                    regulationRepository.getAllRegionRules(
                        regionId = regionId
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
                    regulationRepository.getActiveFishingBans(
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
            compareBy(
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



    // --------------------------------------------------
    // UI STATE
    // --------------------------------------------------

    // --------------------------------------------------
// UI STATE
// --------------------------------------------------

    val uiState: StateFlow<FishLimitUiState> =
        combine(
            baseState,
            regionRulesFlow,
            allRegionRulesFlow,
            fishingBanFlow,
            isDetectingRegion,
            errorMessage
        ) { values: Array<Any?> ->

            val state = values[0] as FishLimitUiState
            val rules = values[1] as List<RegionRule>
            val allRules = values[2] as List<RegionRule>
            val bans = values[3] as List<FishingBan>
            val detectingRegion = values[4] as Boolean
            val error = values[5] as String?

            state.copy(
                regionRules = rules,
                allRegionRules = allRules,
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
    // REGION SELECTION
    // --------------------------------------------------

    fun selectRegion(regionId: Long) {

        // Сначала запрещаем автоматическое изменение региона.
        savedStateHandle[REGION_SELECTED_MANUALLY] = true


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

        if (regionSelectedManually.value
        ) {
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

                    is LocationResult.Success -> {
                        locationResult
                    }

                    else -> {
                        errorMessage.value =
                            "Не удалось определить местоположение. Выберите регион вручную."

                        return@launch
                    }
                }


                // 2. Проверяем, не выбрал ли пользователь регион.

                if (regionSelectedManually.value
                    ||
                    selectedRegionId.value != null
                ) {
                    return@launch
                }

                // 3. Определяем название региона по координатам.

                val regionName = geocoderService.getRegion(
                    lat = location.latitude,
                    lon = location.longitude
                ) ?: run {
                    errorMessage.value =
                        "Не удалось определить регион по координатам."

                    return@launch
                }


                // 4. Повторно проверяем актуальность выбора.

                if (regionSelectedManually.value
                    ||
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
                    !regionSelectedManually.value &&
                    selectedRegionId.value == null
                ) {
                    savedStateHandle[SELECTED_REGION_ID] = region.id
                }

            } catch (e: CancellationException) {
                // Отмена корутины должна распространяться дальше.
                throw e

            } catch (_: Exception) {
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

    private fun launchDatabaseOperation(
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                operation()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorMessage.value =
                    "Не удалось выполнить операцию. Попробуйте ещё раз."
            }
        }
    }

    // --------------------------------------------------
    // DAILY ALLOWANCE CRUD
    // --------------------------------------------------

    fun insertDailyAllowance(allowance: DailyAllowance) {
        launchDatabaseOperation {
            repository.insertDailyAllowance(allowance)
        }
    }

    fun updateDailyAllowance(allowance: DailyAllowance) {
        launchDatabaseOperation {
            repository.updateDailyAllowance(allowance)
        }
    }

    fun deleteDailyAllowance(allowance: DailyAllowance) {
        launchDatabaseOperation {
            repository.deleteDailyAllowance(allowance)
        }
    }


    // --------------------------------------------------
    // FISHING BAN CRUD
    // --------------------------------------------------

fun insertFishingBan(ban: FishingBan) {
    launchDatabaseOperation {
        regulationRepository.insertFishingBan(ban)
    }
}

    fun updateFishingBan(ban: FishingBan) {
        launchDatabaseOperation {
            regulationRepository.updateFishingBan(ban)
        }
    }

    fun deleteFishingBan(ban: FishingBan) {
        launchDatabaseOperation {
            regulationRepository.deleteFishingBan(ban)
        }
    }


    // --------------------------------------------------
    // REGION RULE CRUD
    // --------------------------------------------------

    fun insertRegionRule(rule: RegionRule) {
        launchDatabaseOperation {
            regulationRepository.createRegionRule(rule)
        }
    }


    fun updateRegionRule(rule: RegionRule) {
        launchDatabaseOperation {
            regulationRepository.updateRegionRule(rule)
        }
    }

    fun deleteRegionRule(rule: RegionRule) {
        launchDatabaseOperation {
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

    suspend fun getOverlappingRules(
        rule: RegionRule
    ): List<RegionRule> {

        return regulationRepository.getOverlappingRules(
            regionId = rule.regionId,
            validFrom = rule.validFrom,
            validUntil = rule.validUntil,
            excludeId = rule.id
        )
    }
// =========================================
// СОЗДАНИЕ НОВОГО ПРАВИЛА
// =========================================

    fun createRegionRule(rule: RegionRule) {
        launchDatabaseOperation {
            regulationRepository.createRegionRule(rule)
        }
    }


// =========================================
// СОЗДАНИЕ РЕДАКЦИИ
// =========================================

    fun createRegionRuleRevision(
        previousRule: RegionRule,
        newRule: RegionRule
    ) {
        launchDatabaseOperation {
            regulationRepository.createRegionRuleRevision(
                previousRule = previousRule,
                newRule = newRule
            )
        }
    }





}

data class FishLimitUiState(
    val fishList: List<Fish> = emptyList(),
    val regionList: List<Region> = emptyList(),
    val selectedRegionId: Long? = null,
    val selectedDate: LocalDate = LocalDate.now(),

    // Правила на выбранную дату
    val regionRules: List<RegionRule> = emptyList(),

    // Полная история правил региона
    val allRegionRules: List<RegionRule> = emptyList(),

    val dailyAllowance: List<DailyAllowance> = emptyList(),
    val fishingBans: List<FishingBan> = emptyList(),
    val isLoading: Boolean = false,
    val isDetectingRegion: Boolean = false,
    val errorMessage: String? = null
)
