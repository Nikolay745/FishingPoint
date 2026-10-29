package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// рыболовный запрет
@Entity(
    tableName = "fishing_ban",
    foreignKeys = [
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("regionId")
    ]
)
data class FishingBan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val regionId: Long,
    // Идентификаторы рыб через TypeConverter
    val fishIds: List<Long> = emptyList(),

    val title: String,
    val description: String? = null,
    val location: String? = null,

    // Срок действия самой записи
    val validFrom: LocalDate,
    val validUntil: LocalDate? = null,

    // Повторяется ли сезон ежегодно
    val seasonRepeatsAnnually: Boolean = true,

    // Для ежегодного сезона
    val seasonStartMonth: Int? = null,
    val seasonStartDay: Int? = null,
    val seasonEndMonth: Int? = null,
    val seasonEndDay: Int? = null,

    // Для разового сезонного запрета
    val seasonStartDate: LocalDate? = null,
    val seasonEndDate: LocalDate? = null
)