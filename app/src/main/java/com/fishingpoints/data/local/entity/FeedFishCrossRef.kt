package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "feed_fish_cross_ref",
    primaryKeys = ["feedId", "fishId"],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE // Автоматически удалит связи при удалении рыбы
        ),
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("feedId"),
        Index("fishId")
    ]
)
data class FeedFishCrossRef (
    val feedId: Long,
    val fishId: Long
)