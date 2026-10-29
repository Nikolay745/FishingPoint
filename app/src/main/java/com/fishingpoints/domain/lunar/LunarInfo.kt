package com.fishingpoints.domain.lunar


data class LunarInfo(
    val phase: MoonPhase,
    val phaseName: String,
    val illumination: Double,
    val biteRating: Int,
    val recommendation: String,
    val coefficient: Double
)