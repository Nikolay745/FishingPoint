package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fishingpoints.data.local.enums.Season


// приманки(прикормки
@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bestSeason: Season,
    val bestForFish: String,
    val description: String
)