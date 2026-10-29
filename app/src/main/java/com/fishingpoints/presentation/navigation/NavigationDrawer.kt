package com.fishingpoints.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fishingpoints.BuildConfig
import com.fishingpoints.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class DrawerScreen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Main : DrawerScreen("main", "\uD83C\uDFE0 Главная", {  })
    object Diary : DrawerScreen("fishing_diary", "\uD83D\uDCD4 Дневник", {  })

    object FishingMap : DrawerScreen("fishingMap", "\uD83D\uDDFA\uFE0F Карта", {  })
    object FishSpecies : DrawerScreen("fish_species", "\uD83D\uDC1F Виды рыб", {  })

    object FishCatchLimits : DrawerScreen("fishingRules", "\uD83D\uDCDC Правила рыбалки", { })

    object Region : DrawerScreen("region", "\uD83C\uDF0D Регионы", {  })
    object Location : DrawerScreen("location", "\uD83D\uDCCD Места рыбалок", {  })
    object Settings : DrawerScreen("settings", "⚙\uFE0F Настройки", { })
    object Baits : DrawerScreen("bait", "\uD83E\uDEB1 Насадки и наживки", {  })

    object Feeds : DrawerScreen("feed", "\uD83E\uDEA3 Прикормки и приманки ", {  })

    object Tackles : DrawerScreen("tackle", "\uD83C\uDFA3 Орудия лова рыбы", {    })

    object Hooks : DrawerScreen("hook", "\uD83E\uDE9D Размеры крючков", {  })

    object Statistics : DrawerScreen("statistic", "\uD83D\uDCCA Статистика", {  })

    object About : DrawerScreen("about", "ℹ\uFE0F О приложении", {  })

    object Security : DrawerScreen("security", "\uD83E\uDDBA Безопасность", { })

    object Equipment : DrawerScreen("equipment", "🎒 Экипировка", { })

}

data class DrawerItemUi(
    val screen: DrawerScreen,
    val count: Int? = null
)


@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    navController: NavController,
    scope: CoroutineScope,
    onExitApp: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val diaryCount by viewModel.diaryCount.collectAsStateWithLifecycle()
    val fishCount by viewModel.fishCount.collectAsStateWithLifecycle()
//    val limitCount by viewModel.limitCount.collectAsStateWithLifecycle()
    val regionCount by viewModel.regionCount.collectAsStateWithLifecycle()
    val locationCount by viewModel.locationCount.collectAsStateWithLifecycle()
    val baitsCount by viewModel.baitsCount.collectAsStateWithLifecycle()
    val feedsCount by viewModel.feedCount.collectAsStateWithLifecycle()
    val version = BuildConfig.VERSION_NAME // версия программы

    val drawerItems = listOf(
        DrawerItemUi(DrawerScreen.Main),

        DrawerItemUi(
            DrawerScreen.FishSpecies,
            fishCount
        ),

        DrawerItemUi(
            DrawerScreen.Baits,
            baitsCount
        ),

        DrawerItemUi(
            DrawerScreen.Feeds,
            feedsCount
        ),

        DrawerItemUi(
            DrawerScreen.Tackles,
//            tacklesCount
        ),

        DrawerItemUi(
            DrawerScreen.Hooks,
        ),

        DrawerItemUi(
            DrawerScreen.Statistics,
        ),

        DrawerItemUi(
            DrawerScreen.Diary, // Дневник
            diaryCount
        ),

        DrawerItemUi(
            DrawerScreen.FishingMap // Карты
        ),

        DrawerItemUi(
            DrawerScreen.Region,
            regionCount
        ),
        DrawerItemUi(
            DrawerScreen.Location,
            locationCount
        ),
        DrawerItemUi(
            DrawerScreen.FishCatchLimits,
//            limitCount
        ),
        DrawerItemUi(DrawerScreen.Equipment),
        DrawerItemUi(DrawerScreen.Security),
        DrawerItemUi(DrawerScreen.Settings),
        DrawerItemUi(DrawerScreen.About)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    // Фоновое изображение
                    Image(
                        painter = painterResource(id = R.drawable.xoper),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop, // Масштабирование, чтобы заполнить область
                                alpha = 1f,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Рыболовный гид",
                            color =  Color.Unspecified,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ваш персональный помощник",
                            color =  Color.Unspecified,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Пункты меню
                LazyColumn( userScrollEnabled = true) {
                    items(drawerItems) { item ->
                        NavigationDrawerItem(
                            icon = item.screen.icon,
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.screen.title)

                                    item.count?.let {
                                        Text(
                                            text = "$it",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                scope.launch {
                                    drawerState.close()

                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                            },
                            label = { Text("Выход") },
                            selected = false,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    onExitApp()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                unselectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedTextColor = MaterialTheme.colorScheme.error,
                                unselectedTextColor = MaterialTheme.colorScheme.error,
                                selectedIconColor = MaterialTheme.colorScheme.error,
                                unselectedIconColor = MaterialTheme.colorScheme.error
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Версия приложения
                        Text(
                            text = version,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                    }
                }


            }
        }
    ) {
        content()
    }
}