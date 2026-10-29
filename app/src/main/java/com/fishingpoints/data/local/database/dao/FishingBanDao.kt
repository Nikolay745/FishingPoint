package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.FishingBan
import kotlinx.coroutines.flow.Flow

@Dao
interface FishingBanDao {

    @Query(
        """
        SELECT * FROM fishing_ban
        WHERE regionId = :regionId
          AND validFrom <= :date
          AND (
              validUntil IS NULL
              OR validUntil >= :date
          )
        ORDER BY validFrom DESC
        """
    )
    fun getFishingBansForDate(
        regionId: Long,
        date: String
    ): Flow<List<FishingBan>>

    @Query(
        """
        SELECT * FROM fishing_ban
        WHERE regionId = :regionId
        ORDER BY validFrom DESC
        """
    )
    fun getAllFishingBans(
        regionId: Long
    ): Flow<List<FishingBan>>

    @Insert
    suspend fun insert(ban: FishingBan): Long

    @Update
    suspend fun update(ban: FishingBan)

    @Delete
    suspend fun delete(ban: FishingBan)
// Для начального заполнения
    @Insert
    suspend fun insertBan(ban: FishingBan): Long

    @Query("SELECT COUNT(*) FROM fishing_ban WHERE regionId = :regionId")
    suspend fun getBansCount(regionId: Long): Int
}