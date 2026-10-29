package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bait_fish_cross_ref",
    primaryKeys = ["baitId", "fishId"],
    foreignKeys = [
        ForeignKey(
            entity = Bait::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("baitId"),
        Index("fishId")
    ]
)
data class BaitFishCrossRef(
    val baitId: Long,
    val fishId: Long
)



