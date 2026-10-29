package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.database.dao.FishingCatchDao
import com.fishingpoints.data.local.database.dao.FishingEntryDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.FishingEntryWithLocation
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.presentation.screens.map.FishingMapRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishingRepository @Inject constructor(
    private val fishingEntryDao: FishingEntryDao,
    private val fishingCatchDao: FishingCatchDao,
    private val dailyAllowanceDao: DailyAllowanceDao,
    private val regionDao: RegionDao
) {

    // ============================================================
    // DAILY ALLOWANCE
    // ============================================================
    fun getDailyAllowanceForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<List<DailyAllowance>> {
        return dailyAllowanceDao.getDailyAllowanceForDate(
            regionId = regionId,
            date = date.toString()
        )
    }
    suspend fun updateDailyAllowance(
        daily: DailyAllowance
    ) = dailyAllowanceDao.updateDailyAllowance(daily)

    suspend fun deleteDailyAllowance(
        daily: DailyAllowance
    ) = dailyAllowanceDao.deleteDailyAllowance(daily)

    suspend fun insertDailyAllowance(
        daily: DailyAllowance
    ): Long =
        dailyAllowanceDao.insertDailyAllowance(daily)


    // ============================================================
    // FISHING ENTRIES
    // ============================================================

    fun getAllEntries(): Flow<List<FishingEntry>> =
        fishingEntryDao.getAllEntries()

    suspend fun getEntryById(
        id: Long
    ): FishingEntry? =
        fishingEntryDao.getEntryById(id)

    suspend fun insertEntry(
        entry: FishingEntry
    ): Long =
        fishingEntryDao.insertEntry(entry)

    suspend fun updateEntry(
        entry: FishingEntry
    ) {
        fishingEntryDao.updateEntry(entry)
    }

    suspend fun deleteEntry(
        entry: FishingEntry
    ) {
        fishingEntryDao.deleteEntry(entry)
    }

    suspend fun deleteEntryById(
        id: Long
    ) {
        fishingEntryDao.deleteEntryById(id)
    }


    // ============================================================
    // ENTRIES + LOCATION + REGION
    // ============================================================

    fun getAllEntriesWithLocation():
            Flow<List<FishingEntryWithLocation>> =

        fishingEntryDao
            .getAllEntriesWithLocation()
            .map { rows ->

                rows.map { row ->

                    FishingEntryWithLocation(
                        entry = row.entry,
                        locationName = row.locationName,
                        regionName = row.regionName
                    )
                }
            }


    // ============================================================
    // REGIONS
    // ============================================================

    fun getAllRegions(): Flow<List<Region>> =
        regionDao.getAllRegions()

    fun getFishingMapRows(): Flow<List<FishingMapRow>> {
        return fishingCatchDao.observeFishingMapRows()
    }
}
