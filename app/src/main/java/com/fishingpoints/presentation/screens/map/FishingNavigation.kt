package com.fishingpoints.presentation.screens.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun navigateToFishingLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    locationName: String
) {
    if (
        !latitude.isFinite() ||
        !longitude.isFinite() ||
        latitude !in -90.0..90.0 ||
        longitude !in -180.0..180.0
    ) {
        Toast.makeText(
            context,
            "Некорректные координаты места рыбалки",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    val uri = Uri.parse(
        "google.navigation:q=$latitude,$longitude"
    )

    val navigationIntent = Intent(
        Intent.ACTION_VIEW,
        uri
    ).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(navigationIntent)
    } catch (_: Exception) {

        // Если Google Maps не установлен,
        // открываем маршрут в браузере.
        val browserUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&destination=$latitude,$longitude" +
                    "&travelmode=driving"
        )

        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            browserUri
        )

        try {
            context.startActivity(browserIntent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Не удалось открыть навигацию",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}