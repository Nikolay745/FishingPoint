package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import kotlinx.coroutines.flow.Flow


@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds ORDER BY name")
    fun getAllFeed(): Flow<List<Feed>>

    @Query("SELECT * FROM feeds WHERE id = :id")
    suspend fun getFeedById(id: Long): Feed?

    @Query("SELECT * FROM feeds WHERE name = :name LIMIT 1")
    suspend fun getFeedByName(name: String): Feed?

    @Query("SELECT COUNT(*) FROM feeds ")
    suspend fun getFeedCount(): Int

    @Insert
    suspend fun insertFeed(feed: Feed): Long

    @Insert
    suspend fun insertFeedFishCrossRefs(refs: List<FeedFishCrossRef>)
    @Update
    suspend fun updateFeed(feed: Feed)

    @Delete
    suspend fun deleteFeed(feed: Feed)

    @Query("SELECT Count(*) FROM feeds ")
    fun getCount(): Flow<Int>

    @Transaction
    @Query(
        """
        SELECT f.*
        FROM feeds f
        INNER JOIN feed_fish_cross_ref r
            ON f.id = r.feedId
        WHERE r.fishId = :fishId
        ORDER BY f.name
    """
    )
    suspend fun getFeedForFish(fishId: Long): List<Feed>

    @Transaction
    @Query(
        """
        SELECT fishId FROM feed_fish_cross_ref 
        WHERE feedId = :feedId
    """
    )
    suspend fun getFishIdsForFeed(feedId: Long): List<Long>

    @Insert
    suspend fun insertFeedFishCrossRef(crossRef: FeedFishCrossRef)

    @Query("SELECT * FROM feed_fish_cross_ref WHERE feedId = :feedId AND fishId = :fishId")
    suspend fun getFeedFishCrossRef(feedId: Long, fishId: Long): FeedFishCrossRef?

    @Query("DELETE FROM feed_fish_cross_ref WHERE feedId = :feedId")
    suspend fun deleteFeedFishCrossRefs(feedId: Long)

    @Query("""
        DELETE FROM feed_fish_cross_ref
        WHERE fishId = :fishId
    """)
    suspend fun deleteFeedsForFish(fishId: Long)

    @Query("""
    SELECT * FROM feed_fish_cross_ref
    WHERE fishId = :fishId
""")
    suspend fun getFeedFishCrossRefsForFish(fishId: Long): List<FeedFishCrossRef>

}