package com.fishingpoints.data.local.database.initializer

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "database_initialization"
)

@Singleton
internal class DatabaseInitializationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseInitializer: DatabaseInitializer
) {
    private val initializationKey =
        booleanPreferencesKey("initialization_completed")

    suspend fun initializeIfNeeded() {
        val preferences = context.dataStore.data.first()

        if (preferences[initializationKey] == true) {
            return
        }

        databaseInitializer.initializeDatabase()

        context.dataStore.edit { settings ->
            settings[initializationKey] = true
        }
    }
}

