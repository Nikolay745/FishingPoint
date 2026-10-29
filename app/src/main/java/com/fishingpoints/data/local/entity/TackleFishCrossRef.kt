package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tackle_fish_cross_ref",
    primaryKeys = ["tackleId", "fishId"],
    foreignKeys = [
        ForeignKey(
            entity = Tackle::class,
            parentColumns = ["id"],
            childColumns = ["tackleId"],
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
        Index(value = ["fishId"])
    ]
)
data class TackleFishCrossRef(
    val tackleId: Long,
    val fishId: Long
)
