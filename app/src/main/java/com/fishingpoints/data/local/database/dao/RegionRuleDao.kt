package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.RegionRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionRuleDao {

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
          AND validFrom <= :date
          AND (
              validUntil IS NULL
              OR validUntil >= :date
          )
        ORDER BY validFrom DESC
        LIMIT 1
        """
    )
    fun getRegionRuleForDate(
        regionId: Long,
        date: String
    ): Flow<RegionRule?>

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
        ORDER BY validFrom DESC
        """
    )
    fun getAllRegionRules(
        regionId: Long
    ): Flow<List<RegionRule>>

    @Insert
    suspend fun insertRegionRule(
        rule: RegionRule
    ): Long

    @Update
    suspend fun updateRegionRule(
        rule: RegionRule
    )

    @Delete
    suspend fun deleteRegionRule(
        rule: RegionRule
    )

   // Для начального заполнения
   @Insert
   suspend fun insertRule(rule: RegionRule): Long

    @Query("SELECT COUNT(*) FROM region_rules WHERE regionId = :regionId")
    suspend fun getRulesCount(regionId: Long): Int

}