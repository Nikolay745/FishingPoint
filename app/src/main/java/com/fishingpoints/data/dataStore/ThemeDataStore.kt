package com.fishingpoints.data.dataStore


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeDataStore @Inject constructor(@get:ApplicationContext private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("theme_mode")

    val themeFlow = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun saveTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
}



enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}