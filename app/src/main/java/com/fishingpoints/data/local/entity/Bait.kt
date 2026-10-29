package com.fishingpoints.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fishingpoints.data.local.enums.BaitType
import com.fishingpoints.data.local.enums.Season

// насадки
@Entity(tableName = "baits",
    indices = [Index(value = ["name"], unique = true)])
data class Bait(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: BaitType,
    val bestSeason: Season,
    val description: String
)

data class BaitWithFish(
    @Embedded
    val bait: Bait,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BaitFishCrossRef::class,
            parentColumn = "baitId",
            entityColumn = "fishId"
        )
    )
    val fish: List<Fish>
)
