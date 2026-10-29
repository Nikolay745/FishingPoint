package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//  улов
@Entity(
    tableName = "fishing_catches",
    foreignKeys = [
        ForeignKey(
            entity = FishingEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bait::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Tackle::class,
            parentColumns = ["id"],
            childColumns = ["tackleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("entryId"),
        Index("fishId"),
        Index("baitId"),
        Index("feedId"),
        Index("tackleId")
    ]
)
data class FishingCatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long,
    val fishId: Long,
    val count: Int,
    val totalWeight: Long?,
    val caughtAt: Long = System.currentTimeMillis(), // Время поимки рыбы
    val avgLength: Int? = null,
    val baitId: Long?,
    val feedId: Long?,
    val tackleId: Long?,
    val hookSize: String? = null
)
