package com.fishingpoints.data.local.database.initializer

import android.content.Context
import com.fishingpoints.data.local.database.dto.FishActivityRulesDto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class FishActivityRulesLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun load(): FishActivityRulesDto {

        val json = context.assets
            .open("fish_activity_rules.json")
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(
            json,
            FishActivityRulesDto::class.java
        )
    }
}

