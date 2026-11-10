package com.fishingpoints.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay


@Entity(
    tableName = "fish_activity_rules",
    foreignKeys = [
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("fishId"),
        Index("regionId")
    ]
)

data class FishActivityRule(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val fishId: Long,

    val temperatureMin: Double? = null,
    val temperatureMax: Double? = null,

    val season: Season? = null,
    val timeOfDay: TimeOfDay? = null,

    val regionId: Long? = null,

    val activity: FeedingActivity,

    val description: String? = null,

    val confidence: RuleConfidence = RuleConfidence.MEDIUM,

    val source: RuleSource = RuleSource.DEFAULT
)
