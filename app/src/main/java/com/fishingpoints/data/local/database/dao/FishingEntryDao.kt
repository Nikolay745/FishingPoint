package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.FishingEntryWithLocationRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FishingEntryDao {

    @Query(
        """
        SELECT *
        FROM fishing_entries
        ORDER BY date DESC
        """
    )
    fun getAllEntries(): Flow<List<FishingEntry>>


    @Query(
        """
        SELECT *
        FROM fishing_entries
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getEntryById(id: Long): FishingEntry?

    @Query(
        """
        SELECT
            fe.*,
            COALESCE(
                l.name,
                'Место не найдено'
            ) AS locationName,
            COALESCE(
                r.name,
                'Без региона'
            ) AS regionName

        FROM fishing_entries AS fe

        LEFT JOIN locations AS l
            ON l.id = fe.locationId

        LEFT JOIN regions AS r
            ON r.id = l.regionId

        ORDER BY fe.date DESC
        """
    )
    fun getAllEntriesWithLocation(): Flow<List<FishingEntryWithLocationRow>>

    @Insert
    suspend fun insertEntry(entry: FishingEntry): Long

    @Update
    suspend fun updateEntry(entry: FishingEntry)


    @Delete
    suspend fun deleteEntry(entry: FishingEntry)


    @Query(
        """
        DELETE FROM fishing_entries
        WHERE id = :id
        """
    )
    suspend fun deleteEntryById(id: Long)


    @Query(
        """
        SELECT COUNT(*)
        FROM fishing_entries
        """
    )
    fun getCount(): Flow<Int>
}



