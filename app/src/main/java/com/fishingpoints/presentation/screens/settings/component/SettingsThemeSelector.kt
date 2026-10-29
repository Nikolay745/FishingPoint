package com.fishingpoints.presentation.screens.settings.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import com.fishingpoints.presentation.screens.settings.ThemeMode

@Composable
fun SettingsThemeSelector(
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    SettingsRadioGroup(
        title = "Тема приложения",
        subtitle = "Выберите оформление",
        icon = Icons.Outlined.Palette,
        options = listOf(
            "Светлая",
            "Тёмная"
        ),
        selectedOption = when (themeMode) {
            ThemeMode.LIGHT -> "Светлая"
            ThemeMode.DARK -> "Тёмная"
        },
        onOptionSelected = { option ->
            onThemeSelected(
                when (option) {
                    "Светлая" -> ThemeMode.LIGHT
                    else -> ThemeMode.DARK
                }
            )
        }
    )
}
