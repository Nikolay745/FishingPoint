package com.fishingpoints.presentation.screens.fishingRule.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishingpoints.presentation.formatting.formatWeight
import com.fishingpoints.data.local.entity.DailyAllowance

@Composable
fun FishLimitCard(
    allowance: DailyAllowance,
    name: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false)  }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                }

                Box {
                    IconButton(
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            }
                        )

                    }
                }
            }

            // Диалог подтверждения удаления
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Удалить запись?") },
                    text = {
                        Text("Вы уверены, что хотите удалить эту запись? Это действие нельзя отменить.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDelete()
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Удалить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // CONTENT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                allowance.minLength?.let {
                    Text(
                        text = "Длина: $it см",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                allowance.weightLimit?.let {
                    Text(
                        text = "Вес: ${formatWeight(it.toLong())}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }


                allowance.countLimit?.let {
                    Text(
                        text = "Кол-во: $it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
