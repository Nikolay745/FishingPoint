package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.Region
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {

    @Query(""" SELECT * FROM regions ORDER BY name COLLATE NOCASE ASC """)
    fun getAllRegions(): Flow<List<Region>>

    @Query(""" SELECT * FROM regions WHERE id = :id LIMIT 1 """)
    suspend fun getRegionById(id: Long): Region?

    @Query(
        """
        SELECT *
        FROM regions
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
    """
    )
    suspend fun getRegionsByNameContains(query: String): List<Region>

    @Query(
        """
        SELECT *
        FROM regions
        WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
        LIMIT 1
    """
    )
    suspend fun getRegionByNameIgnoreCase(name: String): Region?

    @Query(
        """
    SELECT *
    FROM regions
    WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
      AND id != :id
    LIMIT 1
    """
    )
    suspend fun getRegionByNameIgnoreCaseExceptId(
        name: String,
        id: Long
    ): Region?


    @Insert
    suspend fun insert(region: Region): Long

    @Update
    suspend fun update(region: Region)

    @Delete
    suspend fun delete(region: Region)

    @Query(
        """
        SELECT COUNT(*)
        FROM regions
    """
    )
    fun getCount(): Flow<Int>

    // Для начального заполнения
    @Insert
    suspend fun insertRegion(region: Region): Long

    @Query("SELECT * FROM regions WHERE name = :name LIMIT 1")
    suspend fun getRegionByName(name: String): Region?
}
