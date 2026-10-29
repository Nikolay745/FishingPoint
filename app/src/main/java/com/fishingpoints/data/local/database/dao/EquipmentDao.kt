package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Query("SELECT * FROM equipment ORDER BY id ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(
        equipment: EquipmentEntity
    )

    @Update
    suspend fun updateEquipment(
        equipment: EquipmentEntity
    )

    @Delete
    suspend fun deleteEquipment(
        equipment: EquipmentEntity
    )

    @Query("UPDATE equipment SET isPacked = 0")
    suspend fun resetAllEquipment()

    @Query("SELECT COUNT(*) FROM equipment")
    suspend fun getEquipmentCount(): Int

    @Query("""
        SELECT COUNT(*) FROM equipment
        WHERE name = :name COLLATE NOCASE
    """)
    suspend fun countByName(name: String): Int

    @Query("""
        SELECT COUNT(*) FROM equipment
        WHERE name = :name COLLATE NOCASE
        AND id != :excludeId
    """)
    suspend fun countByNameExcludingId(
        name: String,
        excludeId: Long
    ): Int
}