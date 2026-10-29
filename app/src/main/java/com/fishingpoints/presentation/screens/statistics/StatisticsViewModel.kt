package com.fishingpoints.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StatisticsUiState(
                selectedMonth = YearMonth.now()
            )
        )

    val uiState: StateFlow<StatisticsUiState> =
        _uiState.asStateFlow()


    private var loadJob: Job? = null


    init {
        loadStatistics()
    }


    // -----------------------------------------------------
    // Изменение периода
    // -----------------------------------------------------

    fun setPeriod(
        period: StatisticsPeriod
    ) {

        if (_uiState.value.period == period) {
            return
        }


        _uiState.update {
            it.copy(
                period = period,
                isLoading = true,
                error = null
            )
        }


        loadStatistics()
    }


    // -----------------------------------------------------
    // Изменение выбранного месяца
    // -----------------------------------------------------

    fun setMonth(
        month: YearMonth
    ) {

        if (_uiState.value.selectedMonth == month) {
            return
        }


        // Не разрешаем выбрать будущее
        if (month > YearMonth.now()) {
            return
        }


        _uiState.update {
            it.copy(
                selectedMonth = month,
                isLoading = true,
                error = null
            )
        }


        loadStatistics()
    }


    // -----------------------------------------------------
    // Загрузка статистики
    // -----------------------------------------------------

    private fun loadStatistics() {

        loadJob?.cancel()


        loadJob = viewModelScope.launch {

            try {

                val state = _uiState.value


                val range = getDateRange(
                    period = state.period,
                    selectedMonth = state.selectedMonth
                )


                val result =
                    repository.getStatistics(
                        startDate = range.start,
                        endDate = range.endExclusive
                    )


                val averageFishPerFishing =
                    if (result.fishingCount > 0) {

                        result.totalFishCount.toDouble() /
                                result.fishingCount

                    } else {
                        0.0
                    }


                val averageFishWeight =
                    if (result.totalFishCount > 0) {

                        result.totalWeight.toDouble() /
                                result.totalFishCount

                    } else {
                        0.0
                    }


                val averageWeightPerFishing =
                    if (result.fishingCount > 0) {

                        result.totalWeight.toDouble() /
                                result.fishingCount

                    } else {
                        0.0
                    }


                _uiState.update {

                    it.copy(
                        isLoading = false,

                        fishingCount =
                            result.fishingCount,

                        catches =
                            result.catches,

                        totalFishCount =
                            result.totalFishCount,

                        totalWeight =
                            result.totalWeight,

                        averageFishPerFishing =
                            averageFishPerFishing,

                        averageFishWeight =
                            averageFishWeight,

                        averageWeightPerFishing =
                            averageWeightPerFishing,

                        fishingStatistics =
                            result.fishingStatistics,

                        dailyStatistics =
                            result.dailyStatistics,

                        locations =
                            result.locations,

                        baits =
                            result.baits,

                        tackles =
                            result.tackles,

                        feeds =
                            result.feeds,

                        bestDay =
                            result.bestDay,

                        bestFishingTime =
                            result.bestFishingTime,

                        error = null
                    )
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _uiState.update {

                    it.copy(
                        isLoading = false,

                        error =
                            e.message
                                ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }


    // -----------------------------------------------------
    // Расчёт диапазона дат
    // -----------------------------------------------------

    private fun getDateRange(
        period: StatisticsPeriod,
        selectedMonth: YearMonth
    ): DateRange {

        val zone =
            ZoneId.systemDefault()

        val today =
            LocalDate.now(zone)


        return when (period) {

            // ---------------------------------------------
            // Конкретный выбранный месяц
            // ---------------------------------------------

            StatisticsPeriod.MONTH -> {

                val startDate =
                    selectedMonth.atDay(1)


                val endDate =
                    selectedMonth
                        .plusMonths(1)
                        .atDay(1)


                DateRange(

                    start =
                        startDate
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli(),

                    endExclusive =
                        endDate
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli()
                )
            }


            // ---------------------------------------------
            // Текущий год
            // ---------------------------------------------

            StatisticsPeriod.YEAR -> {

                val startDate =
                    today.withDayOfYear(1)


                val endDate =
                    startDate.plusYears(1)


                DateRange(

                    start =
                        startDate
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli(),

                    endExclusive =
                        endDate
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli()
                )
            }


            // ---------------------------------------------
            // Всё время
            // ---------------------------------------------

            StatisticsPeriod.ALL_TIME -> {

                DateRange(

                    start = 0L,

                    endExclusive =
                        today
                            .plusDays(1)
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli()
                )
            }
        }
    }


    // -----------------------------------------------------
    // Переход к записи лучшего дня
    // -----------------------------------------------------

    fun getEntryIdForBestDay(
        onResult: (Long?) -> Unit
    ) {

        val date =
            _uiState.value.bestDay?.date


        if (date == null) {

            onResult(null)

            return
        }


        viewModelScope.launch {

            try {

                val entryId =
                    repository.getEntryIdForDate(
                        date
                    )

                onResult(entryId)

            } catch (e: Exception) {

                onResult(null)
            }
        }
    }
}




///////////////////////////////////////////////////////////
data class StatisticsResult(
    val fishingCount: Int,
    val catches: List<CatchStatisticsRow>,
    val totalFishCount: Long,
    val totalWeight: Long,
    val fishingStatistics: List<FishingStatistics>,
    val dailyStatistics: List<DailyCatchStatistics>,
    val locations: List<LocationStatisticsRow>,
    val baits: List<BaitStatisticsRow>,
    val tackles: List<TackleStatisticsRow>,
    val feeds: List<FeedStatisticsRow>,
    val bestDay: BestDayStatistics?,
    val bestFishingTime: BestFishingTimeStatistics?
)

enum class StatisticsPeriod {
    MONTH,
    YEAR,
    ALL_TIME
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val period: StatisticsPeriod = StatisticsPeriod.MONTH,
    val selectedMonth: YearMonth = YearMonth.now(),
    val fishingCount: Int = 0,
    val catches: List<CatchStatisticsRow> = emptyList(),
    val totalFishCount: Long = 0,
    val totalWeight: Long = 0,
    val averageFishPerFishing: Double = 0.0,
    val averageFishWeight: Double = 0.0,
    val averageWeightPerFishing: Double = 0.0,
    val fishingStatistics: List<FishingStatistics> = emptyList(),
    val dailyStatistics: List<DailyCatchStatistics> = emptyList(),
    val locations: List<LocationStatisticsRow> = emptyList(),
    val baits: List<BaitStatisticsRow> = emptyList(),
    val tackles: List<TackleStatisticsRow> = emptyList(),
    val feeds: List<FeedStatisticsRow> = emptyList(),
    val bestDay: BestDayStatistics? = null,
    val bestFishingTime: BestFishingTimeStatistics? = null,
    val error: String? = null
)

data class BestFishingStatistics(
    val entryId: Long,
    val date: Long,
    val locationName: String?,
    val fishCount: Int,
    val totalWeight: Long
)

data class FishingDayStatistics(
    val date: Long,
    val fishCount: Int,
    val totalWeight: Long
)

data class CatchStatisticsRow(
    val fishId: Long,
    val fishName: String,
    val imageUrl: String?,
    val catchCount: Int,
    val totalWeight: Long
)

data class LocationStatisticsRow(
    val locationId: Long,
    val locationName: String,
    val regionName: String,
    val fishingCount: Int,
    val fishCount: Int,
    val totalWeight: Long
)

data class BaitStatisticsRow(
    val baitId: Long,
    val baitName: String,
    val fishingCount: Int,
    val fishCount: Int,
    val totalWeight: Long
)

data class TackleStatisticsRow(
    val tackleId: Long,
    val tackleName: String,
    val fishingCount: Int,
    val fishCount: Int,
    val totalWeight: Long
)

data class FeedStatisticsRow(
    val feedId: Long,
    val feedName: String,
    val fishingCount: Int,
    val fishCount: Int,
    val totalWeight: Long
)

data class BestDayStatistics(
    val date: Long,
    val fishCount: Int,
    val totalWeight: Long,
    val fishingCount: Int
)


data class FishingStatistics(
    val entryId: Long,
    val date: Long,
    val fishCount: Int,
    val totalWeight: Long,
    val zoneId: ZoneId
)

data class DailyCatchStatistics(
    val date: Long,
    val fishCount: Int,
    val fishingCount: Int,
    val totalWeight: Long
)

data class DateRange(
    val start: Long,
    val endExclusive: Long
)

data class BestFishingTimeStatistics(
    val startHour: Int,
    val endHour: Int,
    val fishCount: Int,
    val totalWeight: Long,
    val fishingCount: Int,
    val catchCount: Int
)

data class CatchTimeStatistics(
    val entryId: Long,
    val caughtAt: Long,
    val fishCount: Int,
    val totalWeight: Long
)


