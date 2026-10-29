package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.StatisticsDao
import com.fishingpoints.presentation.screens.statistics.BestDayStatistics
import com.fishingpoints.presentation.screens.statistics.BestFishingTimeStatistics
import com.fishingpoints.presentation.screens.statistics.CatchTimeStatistics
import com.fishingpoints.presentation.screens.statistics.DailyCatchStatistics
import com.fishingpoints.presentation.screens.statistics.FishingStatistics
import com.fishingpoints.presentation.screens.statistics.StatisticsResult
import java.time.Instant
import java.time.ZoneId
import kotlin.collections.component1
import kotlin.collections.component2


class StatisticsRepository(
    private val statisticsDao: StatisticsDao
) {
    suspend fun getStatistics(
        startDate: Long,
        endDate: Long
    ): StatisticsResult {

        val fishingCount = statisticsDao.getFishingCount(
            startDate = startDate,
            endDate = endDate
        )

        val catches = statisticsDao.getCatchStatistics(
            startDate = startDate,
            endDate = endDate
        )

        val totalFishCount =
            statisticsDao.getTotalFishCount(
                startDate = startDate,
                endDate = endDate
            ) ?: 0

        val totalWeight =
            statisticsDao.getTotalWeight(
                startDate = startDate,
                endDate = endDate
            ) ?: 0L

        val fishingStatistics =
            statisticsDao.getFishingStatistics(
                startDate = startDate,
                endDate = endDate
            )

        val catchTimeStatistics =
            statisticsDao.getCatchTimeStatistics(
                startDate = startDate,
                endDate = endDate
            )

        val dailyStatistics =  groupFishingByDay(fishingStatistics)

        val bestDay = dailyStatistics
            .maxWithOrNull(
                compareBy<DailyCatchStatistics> { it.fishCount }
                    .thenBy { it.totalWeight }
            )
            ?.let {
                BestDayStatistics(
                    date = it.date,
                    fishCount = it.fishCount,
                    fishingCount = it.fishingCount,
                    totalWeight = it.totalWeight
                )
            }

        val bestFishingTime =
            calculateBestFishingTime(catchTimeStatistics)


        val locations =
            statisticsDao.getLocationStatistics(
                startDate = startDate,
                endDate = endDate
            )

        val baits =
            statisticsDao.getBaitStatistics(
                startDate = startDate,
                endDate = endDate
            )

        val tackles =
            statisticsDao.getTackleStatistics(
                startDate = startDate,
                endDate = endDate
            )

        val feeds =
            statisticsDao.getFeedStatistics(
                startDate = startDate,
                endDate = endDate
            )



        return StatisticsResult(
            fishingCount = fishingCount,
            catches = catches,
            totalFishCount = totalFishCount.toLong(),
            totalWeight = totalWeight,
            fishingStatistics = fishingStatistics,
            dailyStatistics = dailyStatistics,
            locations = locations,
            baits = baits,
            tackles = tackles,
            feeds = feeds,
            bestDay = bestDay,
            bestFishingTime = bestFishingTime
        )
    }

    private fun groupFishingByDay(
        statistics: List<FishingStatistics>
    ): List<DailyCatchStatistics> {

        val zone = ZoneId.systemDefault()

        return statistics
            .groupBy { item ->
                Instant
                    .ofEpochMilli(item.date)
                    .atZone(zone)
                    .toLocalDate()
            }
            .map { (date, items) ->

                DailyCatchStatistics(
                    date = date
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli(),
                    fishingCount = items.size,
                    fishCount = items.sumOf {
                        it.fishCount
                    },

                    totalWeight = items.sumOf {
                        it.totalWeight
                    }
                )
            }
            .sortedBy { it.date }
    }

    suspend fun getEntryIdForDate(date: Long): Long? {

        val zone = ZoneId.systemDefault()

        val localDate = Instant
            .ofEpochMilli(date)
            .atZone(zone)
            .toLocalDate()

        val startOfDay = localDate
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val endOfDay = localDate
            .plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return statisticsDao.getBestEntryIdForDay(
            startOfDay = startOfDay,
            endOfDay = endOfDay
        )
    }

    private fun calculateBestFishingTime(
        catches: List<CatchTimeStatistics>
    ): BestFishingTimeStatistics? {

        if (catches.isEmpty()) {
            return null
        }

        val zone = ZoneId.systemDefault()

        return catches
            .groupBy { item ->
                Instant
                    .ofEpochMilli(item.caughtAt)
                    .atZone(zone)
                    .hour
            }
            .map { (hour, items) ->

                BestFishingTimeStatistics(
                    startHour = hour,
                    endHour = (hour + 1) % 24,
                    fishCount = items.sumOf { it.fishCount },
                    totalWeight = items.sumOf { it.totalWeight },
                    fishingCount = items
                        .map { it.entryId }
                        .distinct()
                        .size,
                    catchCount = items.size
                )
            }
            .maxWithOrNull(
                compareBy<BestFishingTimeStatistics> {
                    it.fishCount
                }.thenBy {
                    it.totalWeight
                }
            )
    }



}
