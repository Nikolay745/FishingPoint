package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fishing_entries_bait_cross_ref",
    primaryKeys = ["baitId", "fishingEntriesId"],
    indices = [
        Index("baitId"),
        Index("fishingEntriesId")
    ]
)
data class FishingEntriesBaitCrossRef(
    val baitId: Long,
    val fishingEntriesId: Long
)