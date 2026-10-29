package com.fishingpoints.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

// экипировка
@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isPacked: Boolean = false
)