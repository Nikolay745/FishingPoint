package com.fishingpoints.presentation.screens.regions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fishingpoints.data.local.entity.Region
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: RegionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    var editingRegion by remember { mutableStateOf<Region?>(null) }
    var regionToDelete by remember { mutableStateOf<Region?>(null) }

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            showDialog = false
            viewModel.clearSaveCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Регионы")
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
                    editingRegion = null
                    viewModel.clearError()
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
            items(uiState.regions) { region ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = region.name,
                            style = MaterialTheme.typography.titleMedium
                        )

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
                                        editingRegion = region
                                        viewModel.clearError()
                                        showDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Удалить",
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
                                        regionToDelete = region
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
        AddEditRegionDialog(
            region = editingRegion,
            errorMessage = uiState.error,
            isSaving = uiState.isSaving,
            onDismiss = {
                viewModel.clearError()
                showDialog = false
            },
            onSave = { name ->
                if (editingRegion == null) {
                    viewModel.addRegion(name)
                } else {
                    viewModel.updateRegion(
                        editingRegion!!.copy(name = name)
                    )
                }
            }
        )
    }

    regionToDelete?.let { region ->
        AlertDialog(
            onDismissRequest = {
                regionToDelete = null
            },
            title = { Text("Удаление") },
            text = { Text("Удалить регион \"${region.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRegion(region)
                        regionToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { regionToDelete = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

}

@Composable
fun AddEditRegionDialog(
    region: Region?,
    errorMessage: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(region) { mutableStateOf(region?.name ?: "") }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                    }
                }
            ) {
                Text(
                    if (isSaving) {
                        "Сохранение..."
                    } else {
                        "Сохранить"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        },
        title = {
            Text(
                if (region == null) {
                    "Добавить регион"
                } else {
                    "Редактировать регион"
                }
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название региона") },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = {
                    errorMessage?.let {
                        Text(it)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
