package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.LocationDao
import com.fishingpoints.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val regionRepository: RegionRepository
) {

    // ============================================================
    // LOCATIONS
    // ============================================================
    fun getAllLocations(): Flow<List<LocationEntity>> = locationDao.getAllLocations()

    suspend fun getAllLocationsList(): List<LocationEntity> =  locationDao.getAllLocationsList()

    suspend fun getLocationById( id: Long ): LocationEntity? = locationDao.getLocationById(id)

    fun getCount(): Flow<Int> = locationDao.getCount()

    suspend fun insert(
        location: LocationEntity
    ): Long =
        locationDao.insertLocation(location)

    suspend fun update(
        location: LocationEntity
    ) {
        locationDao.update(location)
    }

    suspend fun delete(
        location: LocationEntity
    ) {
        locationDao.delete(location)
    }

    suspend fun getLocationByName(
        name: String
    ): LocationEntity? =
        locationDao.getLocationByName(name)

    suspend fun getLocationsByNameContains(
        query: String
    ): List<LocationEntity> =
        locationDao.getLocationsByNameContains(query)


    // ============================================================
    // CREATE LOCATION
    // ============================================================

    suspend fun createLocation(
        name: String,
        lat: Double,
        lon: Double,
        zoneId: ZoneId,
        regionName: String
    ): Long {

        val normalizedName =
            name
                .trim()
                .replace(Regex("\\s+"), " ")

        require(normalizedName.isNotBlank()) {
            "Название места не может быть пустым"
        }

        val regionId =
            regionRepository.getOrCreateRegion(
                regionName
            )

        val location =
            LocationEntity(
                name = normalizedName,
                regionId = regionId,
                lat = lat,
                lon = lon,
                zoneId = zoneId
            )

        return locationDao.insertLocation(
            location
        )
    }


    // ============================================================
    // REGIONS
    // ============================================================

    suspend fun getOrCreateRegion(
        name: String
    ): Long =
        regionRepository.getOrCreateRegion(name)
}
