package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.FishingCatch
import com.fishingpoints.presentation.screens.diary.CatchWithDetails
import com.fishingpoints.presentation.screens.map.FishingMapRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FishingCatchDao {

    @Query(
        """
        SELECT 
            fc.*,
            f.name AS fishName,
            b.name AS baitName,
            fd.name AS feedName,
            td.name AS tackleName
        FROM fishing_catches fc
        INNER JOIN fish f ON fc.fishId = f.id
        LEFT JOIN baits b ON fc.baitId = b.id
        LEFT JOIN feeds fd ON fc.feedId = fd.id
        LEFT JOIN tackles td ON fc.tackleId= td.id
        WHERE fc.entryId = :entryId
        ORDER BY fc.caughtAt DESC
        """
    )
    fun getCatchesWithDetails(
        entryId: Long
    ): Flow<List<CatchWithDetails>>


    @Query("SELECT COUNT(*) FROM fishing_entries")
    fun getCount(): Flow<Int>


    @Insert
    suspend fun insertCatch(catch: FishingCatch): Long

    @Update
    suspend fun updateCatch(catch: FishingCatch)


    @Delete
    suspend fun deleteCatch(catch: FishingCatch)


    @Query(
        """
        SELECT 
            fs.name,
            SUM(fc.count) AS totalCount,
            SUM(fc.totalWeight) AS totalWeight
        FROM fishing_catches fc
        JOIN fish fs 
            ON fc.fishId = fs.id
        GROUP BY fs.id
        ORDER BY totalCount DESC
        """
    )
    fun getStatistics(): Flow<List<CatchStatistics>>

    @Query(
        """
    SELECT
        l.id AS locationId,
        l.name AS locationName,
        l.lat AS latitude,
        l.lon AS longitude,

        e.id AS entryId,
        e.date AS entryDateMillis,
        e.notes AS entryNotes,

        f.name AS fishName,
        c.count AS fishCount,
        c.totalWeight AS totalWeightGrams,
        c.avgLength AS avgLengthCm,
        c.caughtAt AS caughtAtMillis

    FROM locations l

    LEFT JOIN fishing_entries e
        ON e.locationId = l.id

    LEFT JOIN fishing_catches c
        ON c.entryId = e.id

    LEFT JOIN fish f
        ON f.id = c.fishId

    ORDER BY
        l.name ASC,
        e.date DESC,
        c.caughtAt DESC
    """
    )
    fun observeFishingMapRows(): Flow<List<FishingMapRow>>
}
