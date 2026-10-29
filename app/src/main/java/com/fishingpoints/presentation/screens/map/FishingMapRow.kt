package com.fishingpoints.presentation.screens.map

data class FishingMapRow(
    val locationId: Long,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,

    val entryId: Long?,
    val entryDateMillis: Long?,
    val entryNotes: String?,

    val fishName: String?,
    val fishCount: Int?,
    val totalWeightGrams: Long?,
    val avgLengthCm: Int?,
    val caughtAtMillis: Long?
)