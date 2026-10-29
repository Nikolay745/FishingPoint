package com.fishingpoints

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.fishingpoints.data.local.database.initializer.DatabaseInitializationManager
import com.fishingpoints.presentation.navigation.FishingGuideApp
import com.fishingpoints.presentation.screens.settings.ThemeMode
import com.fishingpoints.ui.theme.FishingPointsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var initializationManager: DatabaseInitializationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            try {
                initializationManager.initializeIfNeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "DatabaseInitialization",
                    "Ошибка инициализации базы данных",
                    e
                )
            }
        }

        setContent {
            // My first change with Git

            val themeViewModel: AppThemeViewModel = hiltViewModel(this)
            val themeMode by themeViewModel.themeMode.collectAsState()
            FishingPointsTheme(
                darkTheme = themeMode == ThemeMode.DARK,
                dynamicColor = false
            ) {
                FishingGuideApp(themeViewModel = themeViewModel)
            }
        }
    }
}
