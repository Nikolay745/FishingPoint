package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_allowance",
    foreignKeys = [
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("fishId"),
        Index("regionId"),
        Index(
            value = ["regionId", "fishId", "validFrom"]
        )
    ]
)
data class DailyAllowance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fishId: Long,
    val regionId: Long,
    val validFrom: LocalDate,           // дата начала действия.
    val validUntil: LocalDate? = null,  // последний день действия включительно
    val minLength: Int? = null,         // минимальная длина в см
    val countLimit: Int? = null,        // лимит штук в день
    val weightLimit: Int? = null,       // лимит веса в граммах
    val notes: String?
)