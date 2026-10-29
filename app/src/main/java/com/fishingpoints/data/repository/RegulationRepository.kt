package com.fishingpoints.data.repository

import com.fishingpoints.core.utils.isActiveOn
import com.fishingpoints.data.local.database.dao.FishingBanDao
import com.fishingpoints.data.local.database.dao.RegionRuleDao
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.RegionRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// для правил и запретов
@Singleton
class RegulationRepository @Inject constructor(
    private val regionRuleDao: RegionRuleDao,
    private val fishingBanDao: FishingBanDao
) {
    suspend fun insertRegionRule(rule: RegionRule): Long = regionRuleDao.insertRegionRule(rule)
    suspend fun updateRegionRule(rule: RegionRule) = regionRuleDao.updateRegionRule(rule)
    suspend fun deleteRegionRule(rule: RegionRule) = regionRuleDao.deleteRegionRule(rule)
    suspend fun insertFishingBan(ban: FishingBan): Long = fishingBanDao.insert(ban)
    suspend fun updateFishingBan(ban: FishingBan) = fishingBanDao.update(ban)
    suspend fun deleteFishingBan(ban: FishingBan) = fishingBanDao.delete(ban)

    fun getFishingBansForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<List<FishingBan>> =
        fishingBanDao.getFishingBansForDate(
            regionId = regionId,
            date = date.toString()
        )



    fun getRegionRuleForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<RegionRule?> =
        regionRuleDao.getRegionRuleForDate(
            regionId = regionId,
            date = date.toString()
        )

    fun getActiveFishingBans(
        regionId: Long,
        date: LocalDate
    ): Flow<List<FishingBan>> {
        return fishingBanDao
            .getFishingBansForDate(
                regionId = regionId,
                date = date.toString()
            )
            .map { bans ->
                bans.filter { ban ->
                    ban.isActiveOn(date)
                }
            }
    }
}

