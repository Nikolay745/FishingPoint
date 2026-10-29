package com.fishingpoints.domain.useCase

import com.fishingpoints.core.location.GeocoderService
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.data.repository.RegionRepository
import java.time.ZoneId
import javax.inject.Inject

class CreateLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val regionRepository: RegionRepository,
    private val geocoderService: GeocoderService
) {

    suspend operator fun invoke(
        name: String,
        lat: Double,
        lon: Double
    ): Long {

        val normalizedName = name.trim()

        require(normalizedName.isNotBlank()) {
            "Введите название места"
        }

        require(lat.isFinite() && lat in -90.0..90.0) {
            "Некорректная широта"
        }

        require(lon.isFinite() && lon in -180.0..180.0) {
            "Некорректная долгота"
        }


        val regionName =
            geocoderService
                .getRegion(lat, lon)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "Неизвестный регион"

        val regionId =
            regionRepository.getOrCreateRegion(regionName)

        return locationRepository.insert(
            LocationEntity(
                name = normalizedName,
                regionId = regionId,
                lat = lat,
                lon = lon,
                zoneId = ZoneId.systemDefault()
            )
        )
    }
}
