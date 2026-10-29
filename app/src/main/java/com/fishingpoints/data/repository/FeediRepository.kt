package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val dao: FeedDao
) {
    val feeds = dao.getAllFeed()

    suspend fun insert(feed: Feed): Long = dao.insertFeed(feed)

    suspend fun update(feed: Feed) = dao.updateFeed(feed)

    suspend fun delete(feed: Feed) = dao.deleteFeed(feed)

    suspend fun getFeedForFish(fishId: Long): List<Feed> = dao.getFeedForFish(fishId)
    suspend fun getFeedByName(name: String): Feed? = dao.getFeedByName(name)

    suspend fun getFishIdsForFeed(feedId: Long): List<Long> = dao.getFishIdsForFeed(feedId)

    suspend fun insertFeedFishCrossRef(crossRef: FeedFishCrossRef) =
        dao.insertFeedFishCrossRef(crossRef)

    suspend fun deleteFeedFishCrossRefs(feedId: Long) =
        dao.deleteFeedFishCrossRefs(feedId)
}