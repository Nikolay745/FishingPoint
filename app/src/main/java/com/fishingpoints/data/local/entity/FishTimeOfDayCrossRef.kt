package com.fishingpoints.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.fishingpoints.data.local.enums.TimeOfDay

@Entity( tableName = "fish_time_of_day_cross_ref",
    primaryKeys = ["fish_id", "timeOfDay"],
    foreignKeys = [
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fish_id"],
            onDelete = ForeignKey.CASCADE // Автоматически удалит связи при удалении рыбы
        )
    ]
)
data class FishTimeOfDayCrossRef(
    @ColumnInfo(name = "fish_id")
    val fishSpeciesId: Long, // Внешний ключ к рыбе
    val timeOfDay: TimeOfDay // Значение из enum
)