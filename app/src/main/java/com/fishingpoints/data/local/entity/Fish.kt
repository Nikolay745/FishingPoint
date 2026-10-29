package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay

@Entity(tableName = "fish",
    indices = [Index(value = ["name"], unique = true)])
data class Fish(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var name: String = "",
    var imageUrl: String? = null,
    var feedingActivity: FeedingActivity? = null,
    var temperatureMin: Int? = null,
    var temperatureMax: Int? = null,
    var pressureMin: Int? = null,
    var pressureMax: Int? = null,
    var description: String? = null,
    var tipsBaits: String? = null,  // насадки/наживки
    var tackles: String? = null,  // орудие лова
    var baitForFish: String? = null,  // прикормка
    var adviceFromFishermen: String? = null, // советы опытных рыболовов
    @Ignore // Это поле не хранится в таблице напрямую, только через FishSeasonCrossRef
    val seasons: List<Season>? = null,         // Используется только для отображения
    @Ignore
    var bestTimeOfDay: List<TimeOfDay>? = null, // Используется только для отображения
)



