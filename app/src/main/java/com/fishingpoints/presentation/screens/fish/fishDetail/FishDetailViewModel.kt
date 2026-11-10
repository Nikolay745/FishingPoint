package com.fishingpoints.presentation.screens.fish.fishDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.repository.BaitRepository
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.data.repository.TackleRepository
import com.fishingpoints.data.repository.WeatherRepository
import com.fishingpoints.domain.service.FishActivityCalculator
import com.fishingpoints.domain.service.WaterTemperatureAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FishDetailViewModel @Inject constructor(
    private val fishRepository: FishRepository,
    private val baitRepository: BaitRepository,
    private val tackleRepository: TackleRepository,
    private val feedDao: FeedDao,
    private val activityCalculator: FishActivityCalculator,
    private val waterTemperatureAnalyzer: WaterTemperatureAnalyzer,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository

) : ViewModel() {
    private var loadJob: Job? = null

    // --- 1. Единый StateFlow ---
    // Приватный изменяемый поток для внутреннего использования
    private val _uiState = MutableStateFlow(FishDetailUiState())

    // Публичный неизменяемый поток для UI
    val uiState: StateFlow<FishDetailUiState> = _uiState.asStateFlow()


    fun loadFishDetails(fishId: Long) {

        if (_uiState.value.loadedFishId == fishId &&
            _uiState.value.fish != null
        ) return

        loadJob?.cancel()

        loadJob = viewModelScope.launch {

            _uiState.update {
                FishDetailUiState(
                    isLoading = true,
                    loadedFishId = null
                )
            }

            try {
                val fishData = fishRepository.getFishById(fishId)

                if (fishData == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Рыба не найдена"
                        )
                    }
                    return@launch
                }

                val seasons = fishRepository.getSeasonsForFish(fishId)
                val bestTimeOfDay = fishRepository.getTimeOfDayForFish(fishId)
                val fish = fishData.copy(
                    seasons = seasons,
                    bestTimeOfDay = bestTimeOfDay
                )

                val baits = baitRepository.getBaitsForFish(fishId)
                val tackles = tackleRepository.getTacklesForFish(fishId)
                val feeds = feedDao.getFeedForFish(fishId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fish = fish,
                        baits = baits,
                        tackles = tackles,
                        feeds = feeds,
                        loadedFishId = fishId
                    )
                }

                updateFishActivity(
                    season = getCurrentSeason(ZoneId.systemDefault()),
                    timeOfDay = getCurrentTimeOfDay(ZoneId.systemDefault()),
                    regionId = null
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Ошибка загрузки рыбы"
                    )
                }
            }
        }
    }

    private fun getWaterTemperature(): Pair<Double?, Boolean> {

        // Сначала проверяем реальную температуру воды.
        if (weatherRepository.isUsingRealWaterTemperature()) {

            val realTemperature =
                weatherRepository.getRealWaterTemperature()

            if (realTemperature != null) {
                return realTemperature to false
            }
        }

        // Если реальной температуры нет, используем температуру воздуха.
        val airTemperature = getCurrentAirTemperature()

        val estimatedTemperature = airTemperature?.let {
            waterTemperatureAnalyzer.estimate(it)
        }

        return estimatedTemperature to (estimatedTemperature != null)
    }


    private var activityJob: Job? = null

    fun updateFishActivity(
        season: Season?,
        timeOfDay: TimeOfDay?,
        regionId: Long?
    ) {

        val fishId = _uiState.value.fish?.id ?: return

        activityJob?.cancel()

        activityJob = viewModelScope.launch {

            _uiState.update {
                it.copy(isActivityLoading = true)
            }

            try {
                val (waterTemperature, isEstimated) =
                    getWaterTemperature()

                val result = activityCalculator.calculate(
                    fishId = fishId,
                    waterTemperature = waterTemperature,
                    season = season,
                    timeOfDay = timeOfDay,
                    regionId = regionId
                )

                _uiState.update {
                    it.copy(
                        activityResult = result,
                        waterTemperature = waterTemperature,
                        isEstimatedTemperature = isEstimated,
                        isActivityLoading = false
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isActivityLoading = false,
                        errorMessage = e.message
                            ?: "Ошибка определения активности"
                    )
                }
            }
        }
    }


    private fun getCurrentAirTemperature(): Double? {
        return weatherRepository
            .getCachedCurrentWeather()
            ?.main
            ?.temp
    }

    suspend fun getFishingRegionId(
        locationId: Long?
    ): Long? {

        if (locationId == null) return null

        return locationRepository
            .getLocationById(locationId)
            ?.regionId
    }

    private fun getCurrentTimeOfDay(
        zoneId: ZoneId
    ): TimeOfDay {

        val hour = ZonedDateTime
            .now(zoneId)
            .hour

        return when (hour) {
            in 0..3 -> TimeOfDay.NIGHT
            in 4..5 -> TimeOfDay.DAWN
            in 6..8 -> TimeOfDay.MORNING
            in 9..15 -> TimeOfDay.DAY
            in 16..17 -> TimeOfDay.AFTERNOON
            in 18..20 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }

    private fun getCurrentSeason(
        zoneId: ZoneId
    ): Season {

        return when (
            ZonedDateTime.now(zoneId).month
        ) {
            Month.MARCH,
            Month.APRIL,
            Month.MAY -> Season.SPRING
            Month.JUNE,
            Month.JULY,
            Month.AUGUST -> Season.SUMMER
            Month.SEPTEMBER,
            Month.OCTOBER,
            Month.NOVEMBER -> Season.AUTUMN

            else -> Season.WINTER
        }
    }


}