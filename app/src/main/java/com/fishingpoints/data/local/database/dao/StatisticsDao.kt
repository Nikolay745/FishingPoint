package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fishingpoints.presentation.screens.statistics.BaitStatisticsRow
import com.fishingpoints.presentation.screens.statistics.CatchStatisticsRow
import com.fishingpoints.presentation.screens.statistics.CatchTimeStatistics
import com.fishingpoints.presentation.screens.statistics.FeedStatisticsRow
import com.fishingpoints.presentation.screens.statistics.FishingStatistics
import com.fishingpoints.presentation.screens.statistics.LocationStatisticsRow
import com.fishingpoints.presentation.screens.statistics.TackleStatisticsRow

@Dao
interface StatisticsDao {

    @Query("""
        SELECT COUNT(*)
        FROM fishing_entries
        WHERE date >= :startDate
          AND date < :endDate
    """)
    suspend fun getFishingCount(
        startDate: Long,
        endDate: Long
    ): Int

    @Query("""
        SELECT
            fish.id AS fishId,
            fish.name AS fishName,
            fish.imageUrl AS imageUrl,
            SUM(fishing_catches.count) AS catchCount,
            SUM(COALESCE(fishing_catches.totalWeight, 0)) AS totalWeight
        FROM fishing_catches
        INNER JOIN fish
            ON fish.id = fishing_catches.fishId
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId
        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate
        GROUP BY
            fish.id,
            fish.name,
            fish.imageUrl
        ORDER BY catchCount DESC
    """)
    suspend fun getCatchStatistics(
        startDate: Long,
        endDate: Long
    ): List<CatchStatisticsRow>

    @Query("""
        SELECT
            SUM(fishing_catches.count)
        FROM fishing_catches
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId
        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate
    """)
    suspend fun getTotalFishCount(
        startDate: Long,
        endDate: Long
    ): Int?


@Query("""
        SELECT SUM(COALESCE(fishing_catches.totalWeight, 0))
        FROM fishing_catches
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId
        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate
    """)
suspend fun getTotalWeight(
    startDate: Long,
    endDate: Long
): Long?

    @Query("""
    SELECT
        fishing_entries.id AS entryId,
        fishing_entries.date AS date,
        SUM(fishing_catches.count) AS fishCount,
        SUM(
            COALESCE(fishing_catches.totalWeight, 0)
        ) AS totalWeight,
        locations.zoneId AS zoneId

    FROM fishing_entries
    INNER JOIN fishing_catches
        ON fishing_catches.entryId = fishing_entries.id

    INNER JOIN locations
        ON locations.id = fishing_entries.locationId

    WHERE fishing_entries.date >= :startDate
      AND fishing_entries.date < :endDate

    GROUP BY fishing_entries.id

    ORDER BY fishing_entries.date ASC
""")
    suspend fun getFishingStatistics(
        startDate: Long,
        endDate: Long
    ): List<FishingStatistics>

    @Query("""
    SELECT
        locations.id AS locationId,
        locations.name AS locationName,
        COALESCE(regions.name, '') AS regionName,
        COUNT(DISTINCT fishing_entries.id) AS fishingCount,
        SUM(fishing_catches.count) AS fishCount,
        SUM(
            COALESCE(fishing_catches.totalWeight, 0)
        ) AS totalWeight

    FROM fishing_entries
    INNER JOIN locations
        ON locations.id = fishing_entries.locationId

    LEFT JOIN regions
        ON regions.id = locations.regionId

    INNER JOIN fishing_catches
        ON fishing_catches.entryId = fishing_entries.id

    WHERE fishing_entries.date >= :startDate
      AND fishing_entries.date < :endDate

    GROUP BY
        locations.id,
        locations.name,
        regions.name

    ORDER BY fishCount DESC
""")
    suspend fun getLocationStatistics(
        startDate: Long,
        endDate: Long
    ): List<LocationStatisticsRow>


    @Query("""
        SELECT
            baits.id AS baitId,
            baits.name AS baitName,
            COUNT(DISTINCT fishing_entries.id) AS fishingCount,
            SUM(fishing_catches.count) AS fishCount,
            SUM(COALESCE(fishing_catches.totalWeight, 0)) AS totalWeight
        FROM fishing_catches
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId

        INNER JOIN baits
            ON baits.id = fishing_catches.baitId

        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate

        GROUP BY
            baits.id,
            baits.name

        ORDER BY fishCount DESC
    """)
    suspend fun getBaitStatistics(
        startDate: Long,
        endDate: Long
    ): List<BaitStatisticsRow>

    @Query("""
        SELECT
            tackles.id AS tackleId,
            tackles.name AS tackleName,
            COUNT(DISTINCT fishing_entries.id) AS fishingCount,
            SUM(fishing_catches.count) AS fishCount,
            SUM(COALESCE(fishing_catches.totalWeight, 0)) AS totalWeight
        FROM fishing_catches
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId
        INNER JOIN tackles
            ON tackles.id = fishing_catches.tackleId
        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate
        GROUP BY
            tackles.id,
            tackles.name
        ORDER BY fishCount DESC
    """)
    suspend fun getTackleStatistics(
        startDate: Long,
        endDate: Long
    ): List<TackleStatisticsRow>

    @Query("""
        SELECT
            feeds.id AS feedId,
            feeds.name AS feedName,
            COUNT(DISTINCT fishing_entries.id) AS fishingCount,
            SUM(fishing_catches.count) AS fishCount,
            SUM(COALESCE(fishing_catches.totalWeight, 0)) AS totalWeight
        FROM fishing_catches
        INNER JOIN fishing_entries
            ON fishing_entries.id = fishing_catches.entryId
        INNER JOIN feeds
            ON feeds.id = fishing_catches.feedId
        WHERE fishing_entries.date >= :startDate
          AND fishing_entries.date < :endDate
        GROUP BY
            feeds.id,
            feeds.name
        ORDER BY fishCount DESC
    """)
    suspend fun getFeedStatistics(
        startDate: Long,
        endDate: Long
    ): List<FeedStatisticsRow>

    // найдёт рыбалку с максимальным уловом за конкретную дату
    @Query("""
    SELECT
        fishing_entries.id
    FROM fishing_entries
    INNER JOIN fishing_catches
        ON fishing_catches.entryId = fishing_entries.id
    WHERE fishing_entries.date >= :startOfDay
      AND fishing_entries.date < :endOfDay
    GROUP BY fishing_entries.id
    ORDER BY
        SUM(fishing_catches.count) DESC,
        SUM(COALESCE(fishing_catches.totalWeight, 0)) DESC
    LIMIT 1
""")
    suspend fun getBestEntryIdForDay(
        startOfDay: Long,
        endOfDay: Long
    ): Long?




    @Query("""
    SELECT
        fishing_catches.entryId AS entryId,
        fishing_catches.caughtAt AS caughtAt,
        fishing_catches.count AS fishCount,
        COALESCE(fishing_catches.totalWeight, 0) AS totalWeight
    FROM fishing_catches
    INNER JOIN fishing_entries
        ON fishing_entries.id = fishing_catches.entryId
    WHERE fishing_entries.date >= :startDate
      AND fishing_entries.date < :endDate
      AND fishing_catches.caughtAt IS NOT NULL
    ORDER BY fishing_catches.caughtAt ASC
""")
    suspend fun getCatchTimeStatistics(
        startDate: Long,
        endDate: Long
    ): List<CatchTimeStatistics>

}