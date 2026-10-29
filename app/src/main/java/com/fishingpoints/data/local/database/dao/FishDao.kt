package com.fishingpoints.data.local.database.dao

import androidx.room.*
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishSeasonCrossRef
import com.fishingpoints.data.local.entity.FishTimeOfDayCrossRef
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {

    @Query("SELECT COUNT(*) FROM fish")
    suspend fun getFishCount(): Int

    @Query("SELECT Count(*) FROM fish")
    fun observeFishCount(): Flow<Int>

    @Query("SELECT * FROM fish ORDER BY name")
    fun getAllFish(): Flow<List<Fish>>

    @Query("SELECT * FROM fish WHERE name IN (:names)")
    suspend fun getFishByNameList(names: List<String>): List<Fish>

    @Query("SELECT * FROM fish WHERE id = :id")
    suspend fun getFishById(id: Long): Fish?

    @Query("SELECT * FROM fish WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getFishByName(name: String): Fish?



    @Query("""
        SELECT * FROM fish 
        JOIN fish_season_cross_ref ON fish.id = fish_season_cross_ref.fish_id 
        WHERE fish_season_cross_ref.season = :season
    """)
    suspend fun getFishBySeason(season: Season): List<Fish>

    @Update
    suspend fun updateFish(fish: Fish)

    @Delete
    suspend fun deleteFish(fish: Fish)

    @Query("""
        SELECT fs.* FROM fish fs
        JOIN bait_fish_cross_ref bf ON fs.id = bf.fishId
        WHERE bf.baitId = :baitId
    """)
    suspend fun getFishByBait(baitId: Long): List<Fish>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFish(fish: Fish): Long

    // Методы для работы с сезонами
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertFishSeasons(crossRefs: List<FishSeasonCrossRef>?)

    @Insert
    suspend fun insertFishSeasonCrossRefs(crossRefs: List<FishSeasonCrossRef>)

    @Query("DELETE FROM fish_season_cross_ref WHERE fish_id = :fishId")
    suspend fun deleteSeasonsForFish(fishId: Long)

    @Query("SELECT season FROM fish_season_cross_ref WHERE fish_id = :fishId")
    suspend fun getSeasonsForFish(fishId: Long): List<Season>


    @Query("DELETE FROM fish_time_of_day_cross_ref WHERE fish_id = :fishId")
    suspend fun deleteTimeOfDayForFish(fishId: Long)

    @Query("SELECT timeOfDay FROM fish_time_of_day_cross_ref WHERE fish_id = :fishId")
    suspend fun getTimeOfDayForFish(fishId: Long): List<TimeOfDay>


    // Унифицированный метод фильтрации
    @Query("""
        SELECT DISTINCT fs.* 
        FROM fish fs
        LEFT JOIN fish_season_cross_ref fscr ON fs.id = fscr.fish_id
        LEFT JOIN fish_time_of_day_cross_ref tdcr ON fs.id = tdcr.fish_id
        WHERE (:query IS NULL OR :query = '' OR fs.name LIKE '%' || :query || '%')
          AND (:season IS NULL OR fscr.season = :season)
          AND (:timeOfDay IS NULL OR tdcr.timeOfDay = :timeOfDay)
        ORDER BY fs.name ASC
    """)
    fun getFilteredFish(
        query: String,
        season: Season? = null,
        timeOfDay: TimeOfDay? = null
    ): Flow<List<Fish>>

    // Дополнительный метод, если нужно фильтровать только по времени суток
   @Query("""
    SELECT DISTINCT fs.* 
    FROM fish fs
    LEFT JOIN fish_time_of_day_cross_ref tdcr ON fs.id = tdcr.fish_id
    WHERE (:query IS NULL OR :query = '' OR fs.name LIKE '%' || :query || '%')
      AND (:timeOfDay IS NULL OR tdcr.timeOfDay = :timeOfDay)
    ORDER BY fs.name ASC
""")
    fun getFilteredFishByTimeOfDay( query: String, timeOfDay: TimeOfDay? ): Flow<List<Fish>>

    @Transaction
    suspend fun upsertFishComplete(
        fish: Fish,
        seasons: List<Season>,
        timeOfDay: List<TimeOfDay>
    ): Long {

        val fishId = if (fish.id == 0L) {
            insertFish(fish)
        } else {
            updateFish(fish)
            fish.id
        }

        // Сначала удаляем старые связи
        deleteSeasonsForFish(fishId)
        deleteTimeOfDayForFish(fishId)

        // Потом записываем актуальные
        if (seasons.isNotEmpty()) {
            insertFishSeasonCrossRefs(
                seasons.map { season ->
                    FishSeasonCrossRef(
                        fishSpeciesId = fishId,
                        season = season
                    )
                }
            )
        }

        if (timeOfDay.isNotEmpty()) {
            insertFishTimeOfDayCrossRefs(
                timeOfDay.map { time ->
                    FishTimeOfDayCrossRef(
                        fishSpeciesId = fishId,
                        timeOfDay = time
                    )
                }
            )
        }

        return fishId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFishTimeOfDayCrossRefs(
        crossRefs: List<FishTimeOfDayCrossRef>
    )


}


data class CatchStatistics(
    val name: String,
    val totalCount: Int,
    val totalWeight: Double
)


