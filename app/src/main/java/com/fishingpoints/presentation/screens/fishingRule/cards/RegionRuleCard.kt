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
import com.fishingpoints.data.local.entity.RegionRule

@Composable
fun RegionRuleCard(
    rule: RegionRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val weightText = remember(rule.dailyWeightLimit) {
        if (rule.dailyWeightLimit > 0) {
            "${rule.dailyWeightLimit / 1000f} кг"
        } else {
            "Норма не задана"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = "Региональные правила",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Дополнительные действия"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Редактировать")
                            },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Удалить")
                            },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            Text(text = "Суточная норма: $weightText")

            if (rule.allowOversizeFish) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Разрешён 1 экземпляр сверх нормы",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Максимум: ${rule.maxDailyNorms} суточных норм",
                style = MaterialTheme.typography.bodyMedium
            )
            rule.notes
                ?.takeIf { it.isNotBlank() }
                ?.let { notes ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall
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

}
