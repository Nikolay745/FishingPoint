package com.fishingpoints.presentation.screens.diary.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.core.location.GeocoderService
import com.fishingpoints.core.location.LocationProvider
import com.fishingpoints.core.location.LocationResult
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, lat: Double, lon: Double) -> Unit,
    locationProvider: LocationProvider,
    geocoderService: GeocoderService
) {
    var locationName by rememberSaveable { mutableStateOf("") }
    var isGettingLocation by remember { mutableStateOf(false) }
    var isGettingAddress by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое место рыбалки") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Название места") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null && locationName.isBlank(),
                    trailingIcon = {
                        if (isGettingAddress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )

                if (currentLocation != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📍 Текущие координаты:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%.6f", currentLocation!!.first)}, " +
                                        String.format("%.6f", currentLocation!!.second),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            isGettingLocation = true
                            errorMessage = null

                            try {
                                when (val result = locationProvider.getCurrentLocation()) {
                                    is LocationResult.Success -> {
                                        currentLocation = result.latitude to result.longitude

                                        // Автоматически получаем адрес
                                        isGettingAddress = true
                                        try {
                                            val address = geocoderService.getAddress(
                                                result.latitude,
                                                result.longitude
                                            )
                                            if (!address.isNullOrBlank() && locationName.isBlank()) {
                                                locationName = address
                                            }
                                        } catch (_: Exception) {
                                            // Игнорируем ошибку геодекодирования
                                        } finally {
                                            isGettingAddress = false
                                        }
                                    }
                                    LocationResult.PermissionDenied -> {
                                        errorMessage = "Нет разрешения на доступ к местоположению"
                                        currentLocation = null
                                    }
                                    LocationResult.Unavailable -> {
                                        errorMessage = "Не удалось получить местоположение"
                                        currentLocation = null
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка: ${e.message}"
                                currentLocation = null
                            } finally {
                                isGettingLocation = false
                            }
                        }
                    },
                    enabled = !isGettingLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isGettingLocation) "Получение..." else "📍 Получить текущее местоположение")
                }
            }
        },
        confirmButton = {

            TextButton(
                onClick = {
                    val location = currentLocation
                    val name = locationName.trim()

                    if (
                        location != null &&
                        name.isNotBlank()
                    ) {
                        onSave(
                            name,
                            location.first,
                            location.second
                        )
                    }
                },

                enabled = locationName.isNotBlank() && currentLocation != null
            ) {
                Text("Сохранить")
            }
        } ,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}