package com.fishingpoints.presentation.screens.diary.cards

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.fishingpoints.presentation.screens.diary.CatchWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun CatchCard(
    catchWithDetails: CatchWithDetails,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val catch = catchWithDetails.catch
    var showDeleteDialog by remember { mutableStateOf(false) }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }


    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "🐟 ${catchWithDetails.fishName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🕐 ${timeFormatter.format(Date(catchWithDetails.catch.caughtAt))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "📊  Количество: ${catch.count} шт.",
                    style = MaterialTheme.typography.bodyMedium
                )
                val totalWeight = catch.totalWeight
                if (totalWeight != null) {
                    Text(
                        text = "⚖️ Вес: $totalWeight г (${
                            String.format(
                                "%.2f",
                                totalWeight / 1000.0
                            )
                        } кг)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                catch.avgLength?.let {
                    Text(
                        text = "📏  Средняя длина: $it см",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val avgWeight =
                    if (catch.count > 0 && catch.totalWeight != null)
                        (catch.totalWeight.toDouble() / catch.count).toInt()
                    else
                        null

                avgWeight?.let {
                    Text(
                        text = "📊  Средний вес: $it г",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                catchWithDetails.tackleName?.let { tackleName ->
                    Text(
                        text = "🎣 Орудия лова: $tackleName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                catchWithDetails.baitName?.let { baitName ->
                    Text(
                        text = "🌾 Насадка: $baitName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                catchWithDetails.feedName?.let { feedName ->
                    Text(
                        text = "🌾 Прикормка: $feedName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }


            }



            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }






        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Удаление")
            },
            text = {
                Text("Вы действительно хотите удалить запись?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
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
