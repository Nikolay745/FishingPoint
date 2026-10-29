package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "regions",
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class Region(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)