package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.TackleDao
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TackleRepository @Inject constructor(
    private val tackleDao: TackleDao
) {

    val tackles: Flow<List<Tackle>> = tackleDao.getAllTackles()

    suspend fun getTackleById(id: Long): Tackle? {
        return tackleDao.getTackleById(id)
    }

    suspend fun getTacklesByName(name: String): Tackle? = tackleDao.getTackleByName(name)

    suspend fun getTacklesForFish(fishId: Long): List<Tackle> {
        return tackleDao.getTacklesForFish(fishId)
    }


    suspend fun insertTackle(tackle: Tackle): Long {
        return tackleDao.insertTackle(tackle)
    }

    suspend fun updateTackle(tackle: Tackle) {
        tackleDao.updateTackle(tackle)
    }

    suspend fun deleteTackle(tackle: Tackle) {
        tackleDao.deleteTackle(tackle)
    }



    suspend fun deleteFishForTackle(tackleId: Long) {
        tackleDao.deleteFishForTackle(tackleId)
    }

    suspend fun saveTackleFish(
        tackleId: Long,
        fishIds: List<Long>
    ) {
        val refs = fishIds.map { fishId ->
            TackleFishCrossRef(
                tackleId = tackleId,
                fishId = fishId
            )
        }

        if (refs.isNotEmpty()) {
            tackleDao.insertTackleFishCrossRefs(refs)
        }
    }
    fun getFishForTackle(tackleId: Long): Flow<List<Fish>> {
        return tackleDao.getFishForTackle(tackleId)
    }


}
