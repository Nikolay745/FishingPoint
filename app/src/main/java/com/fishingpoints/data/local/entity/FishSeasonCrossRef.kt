package com.fishingpoints.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.fishingpoints.data.local.enums.Season

@Entity( tableName = "fish_season_cross_ref",
    primaryKeys = ["fish_id", "season"],
    foreignKeys = [
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fish_id"],
            onDelete = ForeignKey.CASCADE // Автоматически удалит связи при удалении рыбы
        )
    ]
)

data class FishSeasonCrossRef(
    @ColumnInfo(name = "fish_id")
    val fishSpeciesId: Long, // Внешний ключ к рыбе
    val season: Season // Значение из enum
)