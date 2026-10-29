package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// региональные правила
@Entity(tableName = "region_rules",
    foreignKeys = [
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("regionId"),
        Index(
            value = ["regionId", "validFrom"]
        )
    ])
data class RegionRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val regionId: Long,
    val validFrom: LocalDate,
    val validUntil: LocalDate? = null,
    val dailyWeightLimit: Int, // граммы
    val allowOversizeFish: Boolean = true,
    val maxDailyNorms: Int = 2,
    val notes: String? = null
)