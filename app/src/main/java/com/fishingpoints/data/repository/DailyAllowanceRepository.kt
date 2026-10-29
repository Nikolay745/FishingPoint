package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.entity.DailyAllowance
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class DailyAllowanceRepository @Inject constructor(
    private val dao: DailyAllowanceDao
) {

    fun getAll(): Flow<List<DailyAllowance>> =
        dao.getAll()

    fun getForRegion(
        regionId: Long
    ): Flow<List<DailyAllowance>> =
        dao.getForRegion(regionId)

    fun getDailyAllowanceForDate(
        regionId: Long,
        date: LocalDate
    ): Flow<List<DailyAllowance>> =
        dao.getDailyAllowanceForDate(
            regionId = regionId,
            date = date.toString()
        )

    suspend fun getById(
        id: Long
    ): DailyAllowance? =
        dao.getById(id)

    fun getCount(): Flow<Int> =
        dao.getCount()

    suspend fun insert(
        dailyAllowance: DailyAllowance
    ): Long =
        dao.insertDailyAllowance(dailyAllowance)

    suspend fun insertAll(
        dailyAllowances: List<DailyAllowance>
    ) =
        dao.insertDailyAllowance(dailyAllowances)

    suspend fun update(
        dailyAllowance: DailyAllowance
    ) =
        dao.updateDailyAllowance(dailyAllowance)

    suspend fun delete(
        dailyAllowance: DailyAllowance
    ) =
        dao.deleteDailyAllowance(dailyAllowance)
}