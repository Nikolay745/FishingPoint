package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.DailyAllowance
import kotlinx.coroutines.flow.Flow
@Dao
interface DailyAllowanceDao {

    // Все нормы
    @Query("SELECT * FROM daily_allowance")
    fun getAll(): Flow<List<DailyAllowance>>

    // Нормы региона
    @Query(
        """
        SELECT * FROM daily_allowance
        WHERE regionId = :regionId
        ORDER BY validFrom DESC
        """
    )
    fun getForRegion(
        regionId: Long
    ): Flow<List<DailyAllowance>>

    // Нормы, действующие на выбранную дату
    @Query(
        """
        SELECT * FROM daily_allowance
        WHERE regionId = :regionId
          AND validFrom <= :date
          AND (
              validUntil IS NULL
              OR validUntil >= :date
          )
        ORDER BY fishId ASC, validFrom DESC
        """
    )
    fun getDailyAllowanceForDate(
        regionId: Long,
        date: String
    ): Flow<List<DailyAllowance>>

    // Получить норму по ID
    @Query(
        """
        SELECT * FROM daily_allowance
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getById(
        id: Long
    ): DailyAllowance?

    // Количество записей
    @Query("SELECT COUNT(*) FROM daily_allowance")
    fun getCount(): Flow<Int>

    // Добавить одну норму
    @Insert
    suspend fun insertDailyAllowance(
        dailyAllowance: DailyAllowance
    ): Long

    // Добавить несколько норм
    @Insert
    suspend fun insertDailyAllowance(
        dailyAllowances: List<DailyAllowance>
    )

    // Обновить норму
    @Update
    suspend fun updateDailyAllowance(
        dailyAllowance: DailyAllowance
    )

    // Удалить норму
    @Delete
    suspend fun deleteDailyAllowance(
        dailyAllowance: DailyAllowance
    )
// Для начального заполнения
    @Insert
    suspend fun insertAllowance(allowance: DailyAllowance): Long

    @Query("SELECT COUNT(*) FROM daily_allowance WHERE regionId = :regionId")
    suspend fun getAllowancesCount(regionId: Long): Int
}