package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZoneId

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("regionId")]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val regionId: Long?,
    val lat: Double,
    val lon: Double,
    val zoneId: ZoneId
)

