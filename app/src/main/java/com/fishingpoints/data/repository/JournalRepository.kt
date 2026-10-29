package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.CatchStatistics
import com.fishingpoints.data.local.database.dao.FishingCatchDao
import com.fishingpoints.data.local.entity.FishingCatch
import com.fishingpoints.presentation.screens.diary.CatchWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// для записей и уловов

@Singleton
class JournalRepository @Inject constructor(
    private val fishingCatchDao: FishingCatchDao,
) {
    fun getCatchesWithDetails(
        entryId: Long
    ): Flow<List<CatchWithDetails>> =
        fishingCatchDao.getCatchesWithDetails(entryId)

    suspend fun insertCatch(
        catch: FishingCatch
    ): Long =
        fishingCatchDao.insertCatch(catch)

    suspend fun updateCatch(
        catch: FishingCatch
    ) =
        fishingCatchDao.updateCatch(catch)

    suspend fun deleteCatch(
        catch: FishingCatch
    ) =
        fishingCatchDao.deleteCatch(catch)

    fun getStatistics(): Flow<List<CatchStatistics>> =
        fishingCatchDao.getStatistics()
}
