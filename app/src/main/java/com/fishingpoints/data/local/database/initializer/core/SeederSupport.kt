package com.fishingpoints.data.local.database.initializer.core


import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Region
import kotlinx.coroutines.flow.first

/** Все рыбы, проиндексированные по точному имени. */
internal suspend fun FishDao.fishByName(): Map<String, Fish> =
    getAllFish().first().associateBy { it.name }

/** Найти регион по имени или создать. */
internal suspend fun RegionDao.getOrCreateRegion(name: String): Region =
    getRegionByName(name) ?: run {
        val id = insertRegion(Region(name = name))
        Region(id = id, name = name)
    }

internal fun warnMissingFish(fishName: String, owner: String) {
    println("WARNING: Рыба '$fishName' не найдена для '$owner'. Запись пропущена.")
}

internal fun warnMissingAllowance(fishName: String) {
    println("WARNING: Рыба '$fishName' не найдена. Региональная норма пропущена.")
}