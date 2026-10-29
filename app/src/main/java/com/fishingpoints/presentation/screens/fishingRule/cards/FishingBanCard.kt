package com.fishingpoints.presentation.screens.fishingRule.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import com.fishingpoints.data.local.entity.FishingBan
import java.time.format.DateTimeFormatter

@Composable
fun FishingBanCard(
    ban: FishingBan,
    fishNames: List<String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    val validFromText = ban.validFrom.format(dateFormatter)
    val validUntilText = ban.validUntil?.format(dateFormatter) ?: "Бессрочно"

    // Ежегодный сезон запрета
    val seasonStartText = "%02d.%02d".format(
        ban.seasonStartDay,
        ban.seasonStartMonth
    )

    val seasonEndText = "%02d.%02d".format(
        ban.seasonEndDay,
        ban.seasonEndMonth
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ЗАГОЛОВОК И МЕНЮ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ban.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = {
                            expanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Меню действий"
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

            HorizontalDivider()

            // ПЕРИОД ДЕЙСТВИЯ ЗАПИСИ
            Text(
                text = "Действует: $validFromText — $validUntilText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (ban.seasonRepeatsAnnually) {
                // Ежегодный сезон

                seasonStartText?.let { date ->
                    Text("Начало сезона: $date")
                }

                seasonEndText?.let { date ->
                    Text("Окончание сезона: $date")
                }
            } else {
                // Разовый сезон

                ban.seasonStartDate?.let { date ->
                    Text("Начало сезона: ${date.format(dateFormatter)}")
                }

                ban.seasonEndDate?.let { date ->
                    Text("Окончание сезона: ${date.format(dateFormatter)}")
                }
            }


            Text(
                text = "Сезон повторяется ежегодно",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // РЫБА
            Text(
                text = "Рыбы: ${
                    fishNames.ifEmpty {
                        listOf("Не указаны")
                    }.joinToString(", ")
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            // МЕСТО
            ban.location
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = "Место: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            // ОПИСАНИЕ
            ban.description
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = "Описание: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
        }
    }

    // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Удалить запрет?")
            },
            text = {
                Text(
                    "Вы уверены, что хотите удалить эту запись? " +
                            "Это действие нельзя отменить."
                )
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
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}