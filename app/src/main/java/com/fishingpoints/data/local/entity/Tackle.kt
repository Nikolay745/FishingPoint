package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fishingpoints.data.local.enums.TackleType

@Entity(tableName = "tackles",
    indices = [Index(value = ["name"], unique = true)])
data class Tackle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TackleType,
    val description: String
)

