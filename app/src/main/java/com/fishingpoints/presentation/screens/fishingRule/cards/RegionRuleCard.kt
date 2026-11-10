package com.fishingpoints.presentation.screens.fishingRule.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.RegionRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable fun RegionRuleCard(
    rule: RegionRule,
    selectedDate: LocalDate,
    onEdit: () -> Unit,
    onCreateRevision: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    val isActive = !selectedDate.isBefore(rule.validFrom) &&
            (rule.validUntil == null ||
                    !selectedDate.isAfter(rule.validUntil))



    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // НАЗВАНИЕ

            Text(
                text = rule.title,
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider()

            Surface(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActive) {
                        "Действует"
                    } else {
                        "Не действует на выбранную дату"
                    },
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }


            // СОДЕРЖАНИЕ

            Text(
                text = rule.description,
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            // ПЕРИОД ДЕЙСТВИЯ

            Text(
                text = "Период действия",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "С: ${rule.validFrom.format(formatter)}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "До: ${
                    rule.validUntil?.format(formatter) ?: "Бессрочно"
                }",
                style = MaterialTheme.typography.bodySmall
            )

            // ДОПОЛНИТЕЛЬНЫЕ ОГРАНИЧЕНИЯ

            if (
                rule.dailyWeightLimit != null ||
                rule.allowOversizeFish != null ||
                rule.maxDailyNorms != null
            ) {

                HorizontalDivider()

                Text(
                    text = "Дополнительные ограничения",
                    style = MaterialTheme.typography.titleSmall
                )

                rule.dailyWeightLimit?.let { weight ->
                    Text(
                        text = "Суточный вес: $weight г"
                    )
                }

                rule.maxDailyNorms?.let { norms ->
                    Text(
                        text = "Количество суточных норм: $norms"
                    )
                }

                rule.allowOversizeFish?.let { allowed ->
                    Text(
                        text = if (allowed) {
                            "Вылов сверх установленного размера разрешён"
                        } else {
                            "Вылов сверх установленного размера запрещён"
                        },
                        color = if (allowed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // ПРИМЕЧАНИЯ

            rule.notes?.takeIf { it.isNotBlank() }?.let { notes ->

                HorizontalDivider()

                Text(
                    text = "Примечания",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // КНОПКИ УПРАВЛЕНИЯ

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать правило"
                    )
                }

                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить правило",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = onCreateRevision
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Создать новую редакцию"
                    )
                }


            }
        }
    }

// ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Удалить правило?")
            },

            text = {
                Text(
                    "Правило «${rule.title}» будет удалено без возможности восстановления."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error
                    )
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
