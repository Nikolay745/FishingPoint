package com.fishingpoints.domain.permissions


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер для работы с разрешениями на уровне бизнес-логики
 *
 * Используется для проверки разрешений в ViewModel и других слоях
 */
@Singleton
class PermissionManager @Inject constructor(
    private val context: Context
) {
    /**
     * Проверяет, есть ли разрешение на определение местоположения
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Получает список разрешений, которые нужно запросить
     */
    fun getPermissionsToRequest(): Array<String> {
        return listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}