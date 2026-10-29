package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TackleDao {

    @Query("SELECT * FROM tackles ORDER BY name")
    fun getAllTackles(): Flow<List<Tackle>>

    @Query("SELECT * FROM tackles WHERE id = :id")
    suspend fun getTackleById(id: Long): Tackle?

    @Query("SELECT * FROM tackles WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTackleByName(name: String): Tackle?

    @Query("SELECT  COUNT(*) FROM tackles ")
    suspend fun getTackleCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTackle(tackle: Tackle): Long

    @Update
    suspend fun updateTackle(tackle: Tackle)

    @Delete
    suspend fun deleteTackle(tackle: Tackle)


    // ---------------------------------------------------------
    // Tackle ↔ Fish
    // ---------------------------------------------------------

    @Query("""
        SELECT t.*
        FROM tackles t
        INNER JOIN tackle_fish_cross_ref tf
            ON t.id = tf.tackleId
        WHERE tf.fishId = :fishId
        ORDER BY t.name
    """)
    suspend fun getTacklesForFish(fishId: Long): List<Tackle>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTackleFishCrossRef(
        crossRef: TackleFishCrossRef
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTackleFishCrossRefs(
        crossRefs: List<TackleFishCrossRef>
    )

    @Query("""
        SELECT *
        FROM tackle_fish_cross_ref
        WHERE tackleId = :tackleId
          AND fishId = :fishId
    """)
    suspend fun getTackleFishCrossRef(
        tackleId: Long,
        fishId: Long
    ): TackleFishCrossRef?

    @Query("""
        DELETE FROM tackle_fish_cross_ref
        WHERE tackleId = :tackleId
          AND fishId = :fishId
    """)
    suspend fun deleteTackleFishCrossRef(
        tackleId: Long,
        fishId: Long
    )

    @Query("""
        DELETE FROM tackle_fish_cross_ref
        WHERE fishId = :fishId
    """)
    suspend fun deleteTacklesForFish(
        fishId: Long
    )


    @Query("""
    DELETE FROM tackle_fish_cross_ref
    WHERE tackleId = :tackleId
""")
    suspend fun deleteFishForTackle(tackleId: Long)

    @Query("""
    SELECT f.*
    FROM fish f
    INNER JOIN tackle_fish_cross_ref tf
        ON f.id = tf.fishId
    WHERE tf.tackleId = :tackleId
    ORDER BY f.name
""")
    fun getFishForTackle(tackleId: Long): Flow<List<Fish>>




}
