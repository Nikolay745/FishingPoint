package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// крючки
@Entity(tableName = "hooks")
data class Hook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val numberRu: String?,
    val sase: String,
    val saseRu: String?,
    val description: String?
)