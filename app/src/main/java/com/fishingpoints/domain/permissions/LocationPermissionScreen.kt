package com.fishingpoints.domain.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Экран запроса разрешений на определение местоположения
 *
 * Отображает:
 * - Статус разрешений
 * - Объяснение необходимости разрешения
 * - Кнопки для действий
 *
 * @param permissionState - состояние разрешений с действиями
 * @param modifier - модификатор
 */

@Composable
fun LocationPermissionScreen(
    permissionState: PermissionStateWithAction,
    onUseDefaultLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка статуса
        Icon(
            imageVector = if (permissionState.hasPermission) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.LocationOff
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (permissionState.hasPermission) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок
        Text(
            text = if (permissionState.hasPermission) {
                "Доступ к геолокации разрешён"
            } else {
                "Требуется доступ к геолокации"
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Описание
        Text(
            text = when {
                permissionState.hasPermission ->
                    "Приложение может использовать ваше местоположение " +
                            "для уточнения прогноза клёва."

                permissionState.isPermissionPermanentlyDenied ->
                    "Доступ к геолокации не предоставлен. " +
                            "Чтобы разрешить его, откройте настройки приложения."

                permissionState.shouldShowRationale ->
                    "Местоположение помогает определить погоду " +
                            "и подходящее время для рыбалки. " +
                            "Оно используется для расчёта прогноза."

                else ->
                    "Разрешите доступ к местоположению, чтобы получать " +
                            "прогноз для вашего региона."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки действий
        when {
            permissionState.hasPermission -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Готово к работе",
                        modifier = Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 12.dp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            permissionState.isPermissionPermanentlyDenied -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = permissionState.openAppSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Открыть настройки")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onUseDefaultLocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Продолжить без геолокации")
                    }
                }
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = permissionState.requestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            if (permissionState.shouldShowRationale) {
                                "Разрешить доступ"
                            } else {
                                "Предоставить доступ"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onUseDefaultLocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Продолжить без геолокации")
                    }
                }
            }
        }




    }
}