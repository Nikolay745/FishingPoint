package com.fishingpoints.presentation.screens.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.entity.Region
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val regions by viewModel.regions.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    var editingLocations by remember { mutableStateOf<LocationEntity?>(null) }
    var locationToDelete by remember { mutableStateOf<LocationEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Места рыбалки")
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingLocations = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(locations) { location ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row {
                                Text(
                                    text = "Название: ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                            }
                            // Регион
                            Row {
                                Text(
                                    text = "Регион: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.getRegionName(location.regionId),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Координаты
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Координаты: ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " ${location.lat},   ${location.lon}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                        var expanded by remember { mutableStateOf(false) }

                        Box { IconButton( onClick = { expanded = true } ) { Icon( imageVector = Icons.Default.MoreVert, contentDescription = "Действия" ) }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Редактировать") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        editingLocations = location
                                        showDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Удалить",
                                            color = Color.Red
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.Red
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        locationToDelete = location
                                    }
                                )
                            }


                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditLocationDialog(
            location = editingLocations,
            regionList = regions,
            onDismiss = {
                showDialog = false
            },
            onSave = { name, lat, lon, regionId ->
                if (editingLocations == null) {
                    viewModel.addLocation(
                        name = name,
                        lat = lat,
                        lon = lon,
                        regionId = regionId
                    )
                } else {
                    viewModel.updateLocation(
                        editingLocations!!.copy(
                            name = name,
                            lat = lat,
                            lon = lon,
                            regionId = regionId
                        )
                    )
                }
                showDialog = false
            }
        )
    }

    locationToDelete?.let { location ->
        AlertDialog(
            onDismissRequest = { locationToDelete = null },
            title = { Text("Удаление") },
            text = { Text( "Удалить место \"${location.name}\"?" ) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLocation(location)
                        locationToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { locationToDelete = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLocationDialog(
    location: LocationEntity?,
    regionList: List<Region>,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf(location?.name ?: "") }
    var lat by remember {
        mutableStateOf(
            location?.lat?.toString() ?: ""
        )
    } // Исправлено: храним как String
    var lon by remember { mutableStateOf(location?.lon?.toString() ?: "") }
    var regionExpanded by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf(regionList.find { it.id == location?.regionId }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val latDouble = lat.toDoubleOrNull()
                    val lonDouble = lon.toDoubleOrNull()
                    if (name.isNotBlank() && latDouble != null && lonDouble != null && selectedRegion != null) {
                        onSave(
                            name.trim(),
                            latDouble,
                            lonDouble,
                            selectedRegion!!.id
                        )
                    }
                }
            ) {
                Text("Сохранить")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        },

        title = {
            Text(
                if (location == null)
                    "Добавить место рыбалки"
                else
                    "Редактировать место рыбалки"
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название места рыбалки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Широта места рыбалки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Долгота места рыбалки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // REGION
                ExposedDropdownMenuBox(
                    expanded = regionExpanded,
                    onExpandedChange = {
                        regionExpanded = !regionExpanded
                    }
                ) {

                    OutlinedTextField(
                        value = selectedRegion?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Регион") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = regionExpanded
                            )
                        },

                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = regionExpanded,
                        onDismissRequest = {
                            regionExpanded = false
                        }
                    ) {
                        regionList.forEach { region ->

                            DropdownMenuItem(
                                text = { Text(region.name) },
                                onClick = {
                                    selectedRegion = region
                                    regionExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

