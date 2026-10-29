package com.fishingpoints.presentation.navigation

import android.app.Activity
import android.content.Context
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fishingpoints.AppThemeViewModel
import com.fishingpoints.presentation.screens.about.AboutScreen
import com.fishingpoints.presentation.screens.bait.BaitsScreen
import com.fishingpoints.presentation.screens.diary.EditEntryScreen
import com.fishingpoints.presentation.screens.diary.EntryDetailScreen
import com.fishingpoints.presentation.screens.diary.FishingDiaryScreen
import com.fishingpoints.presentation.screens.feed.FeedScreen
import com.fishingpoints.presentation.screens.fish.fishDetail.FishDetailScreen
import com.fishingpoints.presentation.screens.fish.editFish.FishEditScreen
import com.fishingpoints.presentation.screens.fish.listFish.FishSpeciesScreen
import com.fishingpoints.presentation.screens.fishingRule.EquipmentScreen
import com.fishingpoints.presentation.screens.fishingRule.FishingRuleScreen
import com.fishingpoints.presentation.screens.hook.HookSizesScreen
import com.fishingpoints.presentation.screens.location.LocationScreen
import com.fishingpoints.presentation.screens.main.FishForecastScreen
import com.fishingpoints.presentation.screens.main.MainScreen
import com.fishingpoints.presentation.screens.main.MainViewModel
import com.fishingpoints.presentation.screens.map.FishingMapScreen
import com.fishingpoints.presentation.screens.regions.RegionScreen
import com.fishingpoints.presentation.screens.security.SecurityScreen
import com.fishingpoints.presentation.screens.settings.SettingsScreen
import com.fishingpoints.presentation.screens.settings.WaterTemperatureSettingsScreen
import com.fishingpoints.presentation.screens.statistics.StatisticsScreen
import com.fishingpoints.presentation.screens.statistics.StatisticsViewModel
import com.fishingpoints.presentation.screens.tackle.TacklesScreen

@Composable
fun FishingGuideApp(themeViewModel: AppThemeViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    NavigationDrawer(
        drawerState = drawerState,
        navController = navController,
        scope = scope,
        onExitApp = { exitApp(context) }
    ) {
        // Кнопка гамбургера в TopAppBar
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {

            composable("main") { backStackEntry ->

                val mainViewModel: MainViewModel = hiltViewModel(
                    backStackEntry
                )

                MainScreen(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope,
                    viewModel = mainViewModel
                )
            }

            composable("fishing_diary") {
                FishingDiaryScreen(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope,
                    viewModel = hiltViewModel()
                )
            }

            // Добавляем экран редактирования
            composable(
                "entry_detail/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
                EntryDetailScreen(
                    navController = navController,
                    entryId = entryId,
                    viewModel = hiltViewModel()
                )
            }
            composable(
                route = "edit_entry/{entryId}"
            ) { backStackEntry ->

                val entryId =
                    backStackEntry.arguments?.getString("entryId")?.toLongOrNull()

                if (entryId != null) {
                    EditEntryScreen(
                        entryId = entryId,
                        navController = navController
                    )
                }
            }

            composable("fish_species") {
                FishSpeciesScreen(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope,
                    viewModel = hiltViewModel()
                )
            }
            composable(
                "fish_detail/{fishId}",
                arguments = listOf(navArgument("fishId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fishId = backStackEntry.arguments?.getLong("fishId") ?: 0L
                FishDetailScreen(
                    navController = navController,
                    fishId = fishId,
                    viewModel = hiltViewModel()
                )
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    themeViewModel = themeViewModel
                )
            }


            composable(
                "fishingRules"
            ) {
                FishingRuleScreen(
                    drawerState = drawerState,
                    scope = scope,
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable("region") {
                RegionScreen(
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("location") {
                LocationScreen(
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("water_temp_settings") {
                WaterTemperatureSettingsScreen(navController = navController)
            }

            composable("bait") {
                BaitsScreen(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("feed") {
                FeedScreen(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("tackle") {
                TacklesScreen(
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("hook") {
                HookSizesScreen(
                    navController = navController

                )
            }

            composable(
                route = "fish_edit?fishId={fishId}",
                arguments = listOf(
                    navArgument("fishId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val fishId = backStackEntry.arguments?.getLong("fishId") ?: -1L
                FishEditScreen(
                    fishId = fishId,
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }

            composable("about") {
                AboutScreen(
                    drawerState = drawerState,
                    scope = scope
                )
            }

            composable("statistic") {
                val viewModel: StatisticsViewModel = hiltViewModel()
                StatisticsScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable("security") {
                SecurityScreen(
                    navController = navController
                )
            }

            composable("equipment") {
                EquipmentScreen(
                    navController = navController
                )
            }

            composable("fishingMap") {
                FishingMapScreen(
                    viewModel = hiltViewModel(),
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "fish_forecast/{fishId}",
                arguments = listOf(
                    navArgument("fishId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val fishId = backStackEntry.arguments
                    ?.getLong("fishId")

                // Получаем MainViewModel главного экрана.
                val mainBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }

                val mainViewModel: MainViewModel = hiltViewModel(
                    mainBackStackEntry
                )

                if (fishId != null) {
                    FishForecastScreen(
                        fishId = fishId,
                        navController = navController,
                        viewModel = mainViewModel
                    )
                }
            }

        }
    }
}

private fun exitApp(context: Context) {
    // Завершаем Activity
    (context as? Activity)?.finishAffinity()
}