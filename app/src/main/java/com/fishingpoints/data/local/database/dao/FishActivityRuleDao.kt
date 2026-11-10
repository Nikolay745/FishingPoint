package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import kotlinx.coroutines.flow.Flow

@Dao
interface FishActivityRuleDao {

    @Query("""
        SELECT * FROM fish_activity_rules
        ORDER BY fishId ASC
    """)
    fun getAllRules(): Flow<List<FishActivityRule>>

    @Query("""
        SELECT * FROM fish_activity_rules
        WHERE fishId = :fishId
    """)
    suspend fun getRulesByFishId(
        fishId: Long
    ): List<FishActivityRule>

    @Query("""
    SELECT * FROM fish_activity_rules
    WHERE fishId = :fishId
    AND (
        regionId IS NULL
        OR regionId = :regionId
    )
    ORDER BY
        CASE
            WHEN regionId IS NOT NULL THEN 1
            ELSE 0
        END DESC,
        id ASC
""")
    suspend fun getRulesByFishAndRegion(
        fishId: Long,
        regionId: Long?
    ): List<FishActivityRule>



    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRule(
        rule: FishActivityRule
    ): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(
        rules: List<FishActivityRule>
    )

    @Update
    suspend fun updateRule(
        rule: FishActivityRule
    )

    @Delete
    suspend fun deleteRule(
        rule: FishActivityRule
    )

    @Query("""
        SELECT COUNT(*) FROM fish_activity_rules
        WHERE source = 'DEFAULT'
    """)
    suspend fun getDefaultRulesCount(): Int

    @Query("DELETE FROM fish_activity_rules")
    suspend fun deleteAllRules()

    @Query("""
    SELECT COUNT(*) FROM fish_activity_rules
    WHERE fishId = :fishId
    AND temperatureMin IS :temperatureMin
    AND temperatureMax IS :temperatureMax
    AND season IS :season
    AND timeOfDay IS :timeOfDay
    AND regionId IS :regionId
    AND id != :excludeId
""")
    suspend fun countDuplicateRules(
        fishId: Long,
        temperatureMin: Double?,
        temperatureMax: Double?,
        season: Season?,
        timeOfDay: TimeOfDay?,
        regionId: Long?,
        excludeId: Long
    ): Int



}
