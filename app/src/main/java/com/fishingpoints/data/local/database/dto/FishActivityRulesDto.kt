package com.fishingpoints.data.local.database.dto

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class FishActivityRulesDto(
    val version: Int,
    val rules: List<FishActivityRuleDto>
)

data class FishActivityRuleDto(
    val fishName: String,
    val temperatureMin: Double?,
    val temperatureMax: Double?,
    val season: String?,
    val timeOfDay: String?,
    val region: String?,
    val activity: String,
    val description: String?,
    val confidence: String?
)

@Singleton
class FishActivityRulesLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun load(): FishActivityRulesDto {

        val json = context.assets
            .open("fish_activity_rules_voronezh.json")
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(
            json,
            FishActivityRulesDto::class.java
        )
    }
}

