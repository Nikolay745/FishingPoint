package com.fishingpoints.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "fishing_entries",
    indices = [
        Index("date"),
        Index("locationId")
    ]
)
data class FishingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val dateEnd: Date?, // Окончание рыбалки
    val locationId: Long,
    val weather: String? = null,
    val notes: String? = null,
    // Фото места рыбалки
    val photoUri: String? = null,
    // Фото улова
    val catchPhotoUri: String? = null
)

data class FishingEntryWithLocation(
    val entry: FishingEntry,
    val locationName: String,
    val regionName: String = ""
)

// Добавляем класс для группировки
data class GroupedEntries(
    val regionName: String,
    val entries: List<FishingEntryWithLocation>
)


data class FishingEntryWithLocationRow(
    @Embedded
    val entry: FishingEntry,

    @ColumnInfo(name = "locationName")
    val locationName: String,

    @ColumnInfo(name = "regionName")
    val regionName: String
)
