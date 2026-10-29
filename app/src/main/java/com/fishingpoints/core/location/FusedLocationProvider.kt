package com.fishingpoints.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocationProvider {

    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override suspend fun getCurrentLocation(): LocationResult {

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return LocationResult.PermissionDenied
        }

        val priority = if (hasFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        return try {
            val current = client
                .getCurrentLocation(priority, null)
                .await()

            val location = current
                ?: client.lastLocation.await()

            if (location != null) {
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                LocationResult.Unavailable
            }

        } catch (_: SecurityException) {
            LocationResult.PermissionDenied
        } catch (_: Exception) {
            LocationResult.Unavailable
        }
    }
}


