//package com.fishingpoints.ui.theme
//
//import android.app.Activity
//import android.os.Build
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.dynamicDarkColorScheme
//import androidx.compose.material3.dynamicLightColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.graphics.Color
//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)
//
//@Composable
//fun FishingPointsTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = FishingLightColors,
//        typography = Typography,
//        content = content
//    )
//}
//
//private val FishingLightColors = lightColorScheme(
//    primary = Color(0xFF286447),
//    onPrimary = Color.White,
//
//    primaryContainer = Color(0xFFD2EBDD),
//    onPrimaryContainer = Color(0xFF123D29),
//
//    secondary = Color(0xFF527A68),
//    onSecondary = Color.White,
//
//    secondaryContainer = Color(0xFFDCEBE2),
//    onSecondaryContainer = Color(0xFF193D2E),
//
//    tertiary = Color(0xFF477C91),
//    onTertiary = Color.White,
//
//    tertiaryContainer = Color(0xFFD9EDF5),
//    onTertiaryContainer = Color(0xFF173C4B),
//
//    background = Color(0xFFF5F8F5),
//    onBackground = Color(0xFF19231D),
//
//    surface = Color(0xFFFFFFFF),
//    onSurface = Color(0xFF19231D),
//
//    surfaceVariant = Color(0xFFE8EEE9),
//    onSurfaceVariant = Color(0xFF46564B),
//
//    outline = Color(0xFF7C8C80),
//    outlineVariant = Color(0xFFD5DED7)
//)

package com.fishingpoints.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Светлая тема
private val FishingLightColors = lightColorScheme(
    primary = Color(0xFF286447),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFD2EBDD),
    onPrimaryContainer = Color(0xFF123D29),

    secondary = Color(0xFF527A68),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFDCEBE2),
    onSecondaryContainer = Color(0xFF193D2E),

    tertiary = Color(0xFF477C91),
    onTertiary = Color.White,

    tertiaryContainer = Color(0xFFD9EDF5),
    onTertiaryContainer = Color(0xFF173C4B),

    background = Color(0xFFF5F8F5),
    onBackground = Color(0xFF19231D),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF19231D),

    surfaceVariant = Color(0xFFE8EEE9),
    onSurfaceVariant = Color(0xFF46564B),

    outline = Color(0xFF7C8C80),
    outlineVariant = Color(0xFFD5DED7)
)

// Тёмная тема
private val FishingDarkColors = darkColorScheme(
    primary = Color(0xFF9BD5B3),
    onPrimary = Color(0xFF103824),

    primaryContainer = Color(0xFF234C37),
    onPrimaryContainer = Color(0xFFD2EBDD),

    secondary = Color(0xFFB0D5BF),
    onSecondary = Color(0xFF20382A),

    secondaryContainer = Color(0xFF293D32),
    onSecondaryContainer = Color(0xFFDCEBE2),

    tertiary = Color(0xFFA4D1E3),
    onTertiary = Color(0xFF193642),

    tertiaryContainer = Color(0xFF243D49),
    onTertiaryContainer = Color(0xFFD9EDF5),

    background = Color(0xFF101713),
    onBackground = Color(0xFFE1E9E2),

    surface = Color(0xFF18211B),
    onSurface = Color(0xFFE1E9E2),

    surfaceVariant = Color(0xFF29332C),
    onSurfaceVariant = Color(0xFFB8C7BB),

    outline = Color(0xFF84978A),
    outlineVariant = Color(0xFF3A483E)
)

@Composable
fun FishingPointsTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        // Динамические цвета Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        // Тёмная тема
        darkTheme -> FishingDarkColors

        // Светлая тема
        else -> FishingLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
