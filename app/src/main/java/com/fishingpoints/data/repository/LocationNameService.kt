package com.fishingpoints.data.repository

import android.content.Context
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class LocationNameService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun getLocationName(latitude: Double, longitude: Double): String = withContext(
        Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Пробуем получить название в порядке приоритета
                val name = when {
                    !address.locality.isNullOrBlank() -> {
                        // Название города/населенного пункта
                        address.locality
                    }
                    !address.subAdminArea.isNullOrBlank() -> {
                        // Район/область
                        address.subAdminArea
                    }
                    !address.adminArea.isNullOrBlank() -> {
                        // Регион/штат
                        address.adminArea
                    }
                    !address.featureName.isNullOrBlank() -> {
                        // Название объекта
                        address.featureName
                    }
                    !address.thoroughfare.isNullOrBlank() -> {
                        // Улица
                        address.thoroughfare
                    }
                    else -> {
                        // Если ничего не найдено, показываем координаты с пояснением
                        formatCoordinates(latitude, longitude)
                    }
                }

                // Добавляем страну для контекста (если есть)
                val country = address.countryName

                if (!country.isNullOrBlank() && name != country) {
                    "$name, $country"
                } else {
                    name
                }
            } else {
                formatCoordinates(latitude, longitude)
            }
        } catch (_: Exception) {
            formatCoordinates(latitude, longitude)
        }
    }


    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format(Locale.US,"📍 %.4f, %.4f", latitude, longitude)
    }
}