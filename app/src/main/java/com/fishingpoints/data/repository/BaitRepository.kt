package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import javax.inject.Inject

class BaitRepository  @Inject constructor(
    private val dao: BaitDao
) {
    val baits = dao.getAllBaitsWithFish()

    suspend fun insert(bait: Bait): Long =  dao.insertBait(bait)
    suspend fun update(bait: Bait)  = dao.updateBait(bait)
    suspend fun delete(bait: Bait) = dao.deleteBait(bait)
    suspend fun getBaitsForFish(fishId: Long): List<Bait> = dao.getBaitsForFish(fishId)
    suspend fun getBaitsByName(name: String): Bait? = dao.getBaitByName(name)
    suspend fun getFishIdsForBait(baitId: Long): List<Long> = dao.getFishIdsForBait(baitId )

    suspend fun insertBaitFishCrossRefs(crossRefs: List<BaitFishCrossRef>) =
        dao.insertBaitFishCrossRefs(crossRefs)

    suspend fun deleteBaitFishCrossRefs(baitId: Long) =
        dao.deleteBaitFishCrossRefs(baitId)

    suspend fun updateBaitWithFish(
        bait: Bait,
        crossRefs: List<BaitFishCrossRef>
    ) {
        dao.updateBaitWithFish(bait, crossRefs)
    }

}

