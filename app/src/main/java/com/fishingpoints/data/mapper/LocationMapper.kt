package com.fishingpoints.data.mapper

import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.domain.Location

fun LocationEntity.toDomain(): Location =
    Location(
        id = id,
        name = name,
        regionId = regionId,
        lat = lat,
        lon = lon,
        zoneId = zoneId
    )

fun Location.toEntity(): LocationEntity =
    LocationEntity(
        id = id,
        name = name,
        regionId = regionId,
        lat = lat,
        lon = lon,
        zoneId = zoneId
    )
