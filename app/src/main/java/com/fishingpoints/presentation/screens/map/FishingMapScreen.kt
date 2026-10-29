package com.fishingpoints.presentation.screens.map

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fishingpoints.core.common.DEFAULT_LAT
import com.fishingpoints.core.common.DEFAULT_LON
import com.fishingpoints.presentation.formatting.formatWeight
import com.fishingpoints.presentation.screens.diary.FishingDiaryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FishingMapScreen(
    viewModel: FishingDiaryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapRows by viewModel.fishingMapRows.collectAsState()
    val mapView = remember { createMapView(context) }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var centeredOnLocations by remember { mutableStateOf(false) }

    // Группируем данные по местам рыбалки.
    val locations = remember(mapRows) {
        mapRows
            .groupBy { it.locationId }
            .values
            .mapNotNull { rows ->
                rows.firstOrNull()
            }
            .filter { location ->
                location.latitude.isFinite() &&
                        location.longitude.isFinite() &&
                        location.latitude in -90.0..90.0 &&
                        location.longitude in -180.0..180.0
            }
    }

    // Находим данные выбранного места.
    val selectedLocationRows = remember(
        mapRows,
        selectedLocationId
    ) {
        mapRows.filter {
            it.locationId == selectedLocationId
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = {
                mapView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { map ->
                // Удаляем старые маркеры.
                map.overlays.removeAll { overlay ->
                    overlay is Marker
                }

                locations.forEach { location ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(
                        location.latitude,
                        location.longitude
                    )

                    marker.title = location.locationName
                    marker.snippet = "Нажмите, чтобы посмотреть историю уловов"

                    marker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    // Открываем Compose-панель при нажатии.
                    marker.setOnMarkerClickListener { clickedMarker, _ ->
                        selectedLocationId = location.locationId
                        map.controller.animateTo(
                            clickedMarker.position
                        )

                        true
                    }

                    map.overlays.add(marker)
                }

                // Первоначальное позиционирование.
                if (
                    locations.isNotEmpty() &&
                    !centeredOnLocations
                ) {
                    val firstLocation = locations.first()
                    map.controller.setZoom(12.0)
                    map.controller.setCenter(
                        GeoPoint(
                            firstLocation.latitude,
                            firstLocation.longitude
                        )
                    )

                    centeredOnLocations = true
                }

                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )
// Кнопка возврата.
        FilledIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 16.dp,
                    top = 32.dp
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Вернуться назад"
            )
        }

        // Кнопки + и -
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 80.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Увеличение масштаба.
            FilledIconButton(
                onClick = {
                    mapView.controller.zoomIn()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Приблизить карту"
                )
            }

            // Уменьшение масштаба.
            FilledIconButton(
                onClick = {
                    mapView.controller.zoomOut()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Отдалить карту"
                )
            }
        }

        // Информация о количестве мест.
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xEE1B2820)
            )
        ) {
            Text(
                text = "📍 Места рыбалки: ${locations.size}",
                color = Color.White,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
            )
        }

        // Подробности выбранного места.
        if (selectedLocationId != null) {
            FishingLocationDetails(
                locationRows = selectedLocationRows,
                onClose = {
                    selectedLocationId = null
                },
                onBuildRoute = {
                    val location = selectedLocationRows.firstOrNull()

                    if (location != null) {
                        navigateToFishingLocation(
                            context = context,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            locationName = location.locationName
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}


private fun createMapView(
    context: Context
): MapView {

    Configuration.getInstance().userAgentValue = context.packageName

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setBuiltInZoomControls(false)
        controller.setZoom(5.0)
        controller.setCenter(
            GeoPoint(
                DEFAULT_LAT,
                DEFAULT_LON
            )
        )
    }
}


@Composable
private fun FishingLocationDetails(
    locationRows: List<FishingMapRow>,
    onClose: () -> Unit,
    onBuildRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val first = locationRows.firstOrNull() ?: return
    val dateFormat = remember {
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF51B2820)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎣 ${first.locationName}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onClose) {
                    Text("Закрыть")
                }
            }

            Text(
                text = String.format(
                    LocalLocale.current.platformLocale,
                    "Координаты: %.5f, %.5f",
                    first.latitude,
                    first.longitude
                ),
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )
            Button(
                onClick = onBuildRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Построить маршрут")
            }



            // Группируем строки по записям о рыбалке.
            val fishingSessions = locationRows
                .filter { it.entryId != null }
                .groupBy { it.entryId!! }
                .values
                .sortedByDescending { session ->
                    session.first().entryDateMillis ?: 0L
                }

            if (fishingSessions.isEmpty()) {
                Text(
                    text = "Пока нет записей о рыбалке в этом месте.",
                    color = Color.White
                )
            }

            fishingSessions.forEachIndexed { index, session ->

                val sessionInfo = session.first()
                val sessionDate = sessionInfo.entryDateMillis
                    ?.let { dateFormat.format(Date(it)) }
                    ?: "Дата не указана"

                Text(
                    text = "Рыбалка: $sessionDate",
                    color = Color(0xFFB9E4C9),
                    style = MaterialTheme.typography.titleSmall
                )

                val fishRows = session.filter { it.fishName != null }

                if (fishRows.isEmpty()) {
                    Text(
                        text = "Уловы пока не добавлены.",
                        color = Color.White
                    )
                } else {
                    fishRows.forEach { fish ->

                        Text(
                            text = "🐟 ${fish.fishName}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = buildString {
                                append("Количество: ")
                                append(fish.fishCount ?: 0)
                                append(" шт.")

                                fish.totalWeightGrams?.let { grams ->
                                    append(" · Вес: ")
                                    append(formatWeight(grams))
                                }
                            },
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        fish.avgLengthCm?.let { length ->
                            Text(
                                text = "Средняя длина: $length см",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                sessionInfo.entryNotes
                    ?.takeIf { it.isNotBlank() }
                    ?.let { notes ->
                        Text(
                            text = "Заметки: $notes",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                if (index < fishingSessions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 8.dp
                        ),
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
