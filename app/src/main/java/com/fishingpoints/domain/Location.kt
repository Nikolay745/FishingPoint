package com.fishingpoints.domain

import java.time.ZoneId

data class Location(
    val id: Long = 0,
    val name: String,
    val regionId: Long?,
    val lat: Double,
    val lon: Double,
    val zoneId: ZoneId
)
