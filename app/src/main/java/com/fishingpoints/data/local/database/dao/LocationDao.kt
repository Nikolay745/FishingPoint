package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fishingpoints.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface LocationDao {
//
//    @Query("""
//        SELECT *
//        FROM locations
//        ORDER BY name COLLATE NOCASE ASC
//    """)
//    fun getAllLocations(): Flow<List<LocationEntity>>
//
//    @Query("""
//        SELECT *
//        FROM locations
//        ORDER BY name COLLATE NOCASE ASC
//    """)
//    suspend fun getAllLocationsList(): List<LocationEntity>
//
//    @Query("""
//        SELECT *
//        FROM locations
//        WHERE id = :id
//        LIMIT 1
//    """)
//    suspend fun getLocationById(id: Long): LocationEntity?
//
//    @Query("""
//        SELECT COUNT(*)
//        FROM locations
//    """)
//    fun getCount(): Flow<Int>
//
//    @Insert
//    suspend fun insertLocation(location: LocationEntity): Long
//
//    @Update
//    suspend fun update(location: LocationEntity)
//
//    @Delete
//    suspend fun delete(location: LocationEntity
//    )
//
//    @Query("""
//        SELECT *
//        FROM locations
//        WHERE name = :name
//        LIMIT 1
//    """)
//    suspend fun getLocationByName(name: String): LocationEntity?
//
//    @Query("""
//        SELECT *
//        FROM locations
//        WHERE name LIKE '%' || :query || '%'
//        ORDER BY name COLLATE NOCASE ASC
//    """)
//    suspend fun getLocationsByNameContains(
//        query: String
//    ): List<LocationEntity>
//}

@Dao
interface LocationDao {

    @Query("""
        SELECT *
        FROM locations
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("""
        SELECT *
        FROM locations
        ORDER BY name COLLATE NOCASE ASC
    """)
    suspend fun getAllLocationsList(): List<LocationEntity>

    @Query("""
        SELECT *
        FROM locations
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getLocationById(id: Long): LocationEntity?

    @Query("""
        SELECT COUNT(*)
        FROM locations
    """)
    fun getCount(): Flow<Int>

    @Insert
    suspend fun insertLocation(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("""
        SELECT *
        FROM locations
        WHERE name = :name
        LIMIT 1
    """)
    suspend fun getLocationByName(name: String): LocationEntity?

    @Query("""
        SELECT *
        FROM locations
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
    """)
    suspend fun getLocationsByNameContains(
        query: String
    ): List<LocationEntity>
}
