package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import com.fishingpoints.data.local.entity.BaitWithFish
import kotlinx.coroutines.flow.Flow

@Dao
interface BaitDao {
    @Query("SELECT * FROM baits ORDER BY name")
    fun getAllBaits(): Flow<List<Bait>>

    @Insert
    suspend fun insertBait(bait: Bait): Long

    @Update
    suspend fun updateBait(bait: Bait)

    @Delete
    suspend fun deleteBait(bait: Bait)

    @Query("SELECT Count(*) FROM baits ")
    fun getCount(): Flow<Int>

    @Query("SELECT Count(*) FROM baits ")
    suspend fun getBaitCount(): Int

    @Query("""
        SELECT b.* FROM baits b
        INNER JOIN bait_fish_cross_ref bf ON b.id = bf.baitId
        WHERE bf.fishId = :fishId
        ORDER BY b.name
    """)
    suspend fun getBaitsForFish(fishId: Long): List<Bait>


    @Query(
        """
        SELECT fishId FROM bait_fish_cross_ref 
        WHERE baitId = :baitId
    """
    )
    suspend fun getFishIdsForBait(baitId: Long): List<Long>


    //  методы для перекрестных ссылок
    @Insert
    suspend fun insertBaitFishCrossRef(crossRef: BaitFishCrossRef)

    @Insert
    suspend fun insertBaitFishCrossRefs(crossRefs: List<BaitFishCrossRef>)

    @Query("SELECT * FROM bait_fish_cross_ref WHERE baitId = :baitId AND fishId = :fishId")
    suspend fun getBaitFishCrossRef(baitId: Long, fishId:Long): BaitFishCrossRef?

    @Query("DELETE FROM bait_fish_cross_ref WHERE baitId = :baitId")
    suspend fun deleteBaitFishCrossRefs(baitId: Long)

    @Query("""
        DELETE FROM bait_fish_cross_ref
        WHERE fishId = :fishId
    """)
    suspend fun deleteBaitsForFish(fishId: Long)

    @Transaction
    suspend fun updateBaitWithFish(
        bait: Bait,
        crossRefs: List<BaitFishCrossRef>
    ) {
        updateBait(bait)
        deleteBaitFishCrossRefs(bait.id)

        if (crossRefs.isNotEmpty()) {
            insertBaitFishCrossRefs(crossRefs)
        }
    }

    @Transaction
    @Query("SELECT * FROM baits ORDER BY name")
    fun getAllBaitsWithFish(): Flow<List<BaitWithFish>>

    @Query("SELECT * FROM baits WHERE id = :id")
    suspend fun getBaitById(id: Long): Bait?

    @Query("SELECT * FROM baits WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getBaitByName(name: String): Bait?


}