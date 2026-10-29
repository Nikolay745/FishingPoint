package com.fishingpoints.core.location

import android.content.Context
import android.location.Geocoder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}

sealed interface LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double
    ) : LocationResult

    data object PermissionDenied : LocationResult

    data object Unavailable : LocationResult
}

interface GeocoderService {
    suspend fun getAddress(
        lat: Double,
        lon: Double
    ): String?

    suspend fun getRegion(
        lat: Double,
        lon: Double
    ): String?
}

@Singleton
class AndroidGeocoderService @Inject constructor(
    @ApplicationContext context: Context
) : GeocoderService {

    private val geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    override suspend fun getAddress(
        lat: Double,
        lon: Double
    ): String = withContext(Dispatchers.IO) {

        runCatching {

            val address = geocoder
                .getFromLocation(lat, lon, 1)
                ?.firstOrNull()
//            Log.d("GEOCODER", "ADDRESS = $address")
//            Log.d("GEOCODER", "LOCALITY = ${address?.locality}")
//            Log.d("GEOCODER", "SUB_ADMIN = ${address?.subAdminArea}")
//            Log.d("GEOCODER", "ADMIN = ${address?.adminArea}")
            address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: "$lat, $lon"

        }.getOrElse {
            Log.e("GEOCODER", "FAILED", it)
            "$lat, $lon"
        }
    }

    override suspend fun getRegion(
        lat: Double,
        lon: Double
    ): String? = withContext(Dispatchers.IO) {

        runCatching {
            geocoder
                .getFromLocation(lat, lon, 1)
                ?.firstOrNull()
                ?.adminArea
        }.getOrNull()
    }
}